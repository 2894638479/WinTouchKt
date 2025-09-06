package dsl

import error.wrapExceptionName
import geometry.Color
import kotlinx.cinterop.invoke
import logger.info
import logger.warning
import platform.windows.LPARAM
import platform.windows.RECT
import platform.windows.SendMessage
import platform.windows.TBM_SETPOS
import platform.windows.TRUE
import wrapper.GuiWindow
import wrapper.height
import wrapper.str
import wrapper.width
import kotlin.math.max
import kotlin.math.roundToInt


@Gui
abstract class GuiScope(
    parent: GuiWindow?, name:String,
    modifier: Modifier,
    alignment: Alignment,
    color:State<Color?> = stateNull(),
    style:Int = 0,
): AbstractGuiComponent(modifier, alignment),State.Scope {
    override var _onDestroy = mutableListOf<()->Unit>()
    private inline fun <T> remapScope(scope: State.Scope, block:GuiScope.()->T):T{
        val rem = _onDestroy
        _onDestroy = scope._onDestroy
        return block().also { _onDestroy = rem }
    }
    private val window = object : GuiWindow(name,
        if(parent == null) modifier else M,
        style,parent
    ){
        val scope = this@GuiScope
        override fun onSize() {
            scope.onSize()
            super.onSize()
        }
        override val scrollableHeight get() = scope.scrollableHeight
        override val scrollableWidth get() = scope.scrollableWidth
        override fun onDestroy(): Boolean {
            destroy()
            children.clear()
            onCommand.clear()
            return super.onDestroy()
        }
    }
    init {
        color.listen(true) { window.backGndColor = it }
    }
    private val top: GuiWindow get() = window.parent ?: window
    fun reLayout(){
        top.onSize()
        onSize()
        top.hwnd.invalidateRect()
    }
    open val scrollableHeight get() = -1
    open val scrollableWidth get() = -1
    override val hwnd get() = window.hwnd
    private var children = mutableListOf<GuiChild>()
    protected val visibleChildren get() = mutableListOf<AbstractGuiComponent>().apply {
        children.forEach { it.addTo(this) }
    }
    override val innerMinH get() = visibleChildren.maxOfOrNull { it.outerMinH } ?: 0
    override val innerMinW get() = visibleChildren.maxOfOrNull { it.outerMinW } ?: 0
    private fun AbstractGuiComponent.addToChild(){ children += this }
    private fun remapChild(block: GuiScope.() -> Unit):List<GuiChild>{
        val rem = children
        children = mutableListOf()
        block()
        return children.also { children = rem }
    }
    abstract fun onSize()
    fun Box(modifier: Modifier = M, alignment: Alignment = A,color:State<Color?> = stateNull(), block: BoxScope.()->Unit){
        BoxScope(modifier, alignment, window,color = color).apply(block).addToChild()
    }
    fun Column(modifier: Modifier = M, alignment: Alignment = A, block: ColumnScope.()->Unit){
        ColumnScope(modifier, alignment, window).apply(block).addToChild()
    }
    fun Row(modifier: Modifier = M, alignment: Alignment = A, block: RowScope.()->Unit){
        RowScope(modifier, alignment, window).apply(block).addToChild()
    }
    fun Button(modifier: Modifier = M, alignment: Alignment = A, text:State<String>,enable:State<Boolean> = stateOf(true), onClick:()->Unit){
        GuiComponent(modifier, alignment, window.button(text.value, onClick)).apply{
            text.listen { hwnd.name = it }
            enable.listen { hwnd.enable(it) }
            hwnd.enable(enable.value)
        }.addToChild()
    }
    fun Edit(modifier: Modifier = M, alignment: Alignment = A, text: State<String>,onEdit:(String)->Unit){
        GuiComponent(modifier, alignment, window.edit(text.value,onEdit)).apply {
            text.listen {
                val sel = hwnd.sel
                hwnd.name = it
                onEdit(it)
                hwnd.sel = sel
            }
        }.addToChild()
    }
    fun Custom(modifier: Modifier = M, alignment: Alignment = A, block:()-> GuiWindow){
        GuiComponent(modifier, alignment, block().hwnd).addToChild()
    }
    fun Text(modifier: Modifier = M, alignment: Alignment = A, text: State<String>,textAlign: Alignment = A.middle()){
        GuiComponent(modifier, alignment, window.text(text.value, textAlign)).apply {
            text.listen { hwnd.name = it }
        }.addToChild()
    }
    fun <T> TrackBar(modifier: Modifier = M, alignment: Alignment = A,value: State<T>,range: ClosedRange<T>,steps:Int = 1000,onChange:(T)->Unit)
    where T:Number,T:Comparable<T>{
        GuiComponent(modifier,alignment,window.trackBar(range,steps,onChange)).apply {
            value.listen(true) {
                val start = range.start.toDouble()
                val end = range.endInclusive.toDouble()
                val value = it.toDouble()
                val progress = (value - start) / (end - start)
                val pos = (progress * steps).roundToInt()
                hwnd.sendMessage(TBM_SETPOS.toUInt(), TRUE.toULong(),pos.toLong())
            }
        }.addToChild()
    }
    fun ScrollableColumn(modifier: Modifier = M, alignment: Alignment = A, block: ScrollableColumnScope.()->Unit){
        ScrollableColumnScope(modifier, alignment, window).apply(block).addToChild()
    }
    fun <T> List(list: MutStateList<T>, item: GuiScope.(T)-> Unit){
        class ListItem(val element:T,val scope: State.Scope,val child: GuiListChild)
        val fullList = GuiListChild(mutableListOf())
        children.add(fullList)
        val items = mutableListOf<ListItem>()
        list.listen(true,object:MutStateList.Listener<T>{
            override fun onAnyChange(list: List<T>) {}
            override fun onAdd(element: T) {
                val scope = MutState.SimpleScope()
                val item = ListItem(element,scope, GuiListChild(mutableListOf()))
                item.child += remapScope(scope){
                    remapChild{ item(element) }
                }
                items += item
                fullList.add(item.child)
                reLayout()
            }
            override fun onRemove(element: T) = wrapExceptionName("removing element from List") {
                val item = items.firstOrNull { it.element == element } ?: error("not found item")
                items.remove(item)
                if(!fullList.remove(item.child)) error("list remove child error")
                item.child.destroyHwnd()
                item.scope.destroy()
                reLayout()
            }
        })
    }

    fun <T> By(state:State<T>,content:(T)-> Unit){
        val list = GuiListChild(mutableListOf())
        children.add(list)
        val scope = MutState.SimpleScope()
        _onDestroy += { scope.destroy() }
        state.listen(true) {
            scope.destroy()
            list.destroyHwnd()
            list.clear()
            list += remapChild {
                remapScope(scope){
                    content(it)
                }
            }
            reLayout()
        }
    }
    value class IfScope(val bool: State<Boolean>)
    fun If(state:State<Boolean>,content:()->Unit) =
        By(state){ if(it) content() }.run { IfScope(state) }
    infix fun IfScope.Else(content:()-> Unit) = By(bool){ if(!it) content() }



    fun split(weight:FloatArray, min:IntArray,forced:IntArray,full:Int):IntArray = wrapExceptionName("split error"){
        require(weight.size == min.size && min.size == forced.size) { "array size not match" }
        if(min.sum() >= full) return min
        val indices = weight.indices
        val size = weight.size
        val results = IntArray(size){ index ->
            forced[index].let {
                if(it == 0) Int.MIN_VALUE else max(it,min[index])
            }
        }
        var sumWeight = 0f
        var remain = 0
        fun calSumWeight(){
            sumWeight = 0f
            for (i in indices){
                if(results[i] == Int.MIN_VALUE) sumWeight += weight[i]
            }
        }
        fun setOtherTo0(){
            for (i in indices) {
                if(results[i] == Int.MIN_VALUE) results[i] = min[i]
            }
        }
        fun calRemain(){
            remain = full
            for (i in indices){
                if(results[i] != Int.MIN_VALUE) remain -= results[i]
            }
        }
        while(true) {
            calSumWeight()
            if (sumWeight == 0f) {
                setOtherTo0()
                break
            }
            calRemain()
            var added = false
            for (i in indices) {
                if (results[i] != Int.MIN_VALUE) continue
                val thisH = (weight[i] / sumWeight * remain)
                if (thisH <= min[i]) {
                    results[i] = min[i]
                    added = true
                }
            }
            if (!added) {
                while (results.find { it == Int.MIN_VALUE } != null) {
                    calSumWeight()
                    if (sumWeight == 0f) {
                        setOtherTo0()
                        break
                    }
                    calRemain()
                    for (i in indices) {
                        if (results[i] != Int.MIN_VALUE) continue
                        val thisH = (weight[i] / sumWeight * remain)
                        results[i] = thisH.toInt()
                        break
                    }
                }
                break
            }
        }
        return results
    }


    fun RECT.placeTB(modifier: Modifier, bound: RECT, align: Alignment,minH:()->Int) {
        val height = if(modifier.height == 0) bound.height else minH()
        if (align.bottom) {
            bottom = bound.bottom
            top = bottom - height
        } else if (align.middleY) {
            top = bound.top + (bound.height - height) / 2
            bottom = top + height
        } else {
            top = 0
            bottom = top + height
        }
    }


    fun RECT.placeLR(modifier: Modifier, bound: RECT, align: Alignment,minW:()->Int) {
        val width = if(modifier.width == 0) bound.width else minW()
        if (align.right) {
            right = bound.right
            left = right - width
        } else if (align.middleX) {
            left = bound.left + (bound.width - width) / 2
            right = left + width
        } else {
            left = 0
            right = width
        }
    }

    fun RECT.padding(modifier: Modifier){
        left += modifier.paddingLeft
        right -= modifier.paddingRight
        top += modifier.paddingTop
        bottom -= modifier.paddingBottom
    }
}