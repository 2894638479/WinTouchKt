package dsl

import error.wrapExceptionName
import geometry.Color
import logger.warning
import platform.windows.RECT
import wrapper.GuiWindow
import wrapper.height
import wrapper.str
import wrapper.width


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
        if(parent == null) modifier.minW else 0,
        if(parent == null) modifier.minH else 0,
        style,parent
    ){
        val scope = this@GuiScope
        override fun onSize() {
            if(parent == null) warning("top window $name onSize ${hwnd.useRect { it.str() }}")
            else warning("$name onSize ${hwnd.useRect { it.str() }}")
            scope.onSize()
            super.onSize()
        }
        override val scrollableHeight get() = scope.scrollableHeight
        override val scrollableWidth get() = scope.scrollableWidth
        override fun onDestroy(): Boolean {
            destroyChild()
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
    fun Box(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(),color:State<Color?> = stateNull(), block: BoxScope.()->Unit){
        BoxScope(modifier, alignment, window,color = color).apply(block).addToChild()
    }
    fun Column(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(), block: ColumnScope.()->Unit){
        ColumnScope(modifier, alignment, window).apply(block).addToChild()
    }
    fun Row(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(), block: RowScope.()->Unit){
        RowScope(modifier, alignment, window).apply(block).addToChild()
    }
    fun Button(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(), text:State<String>,enable:State<Boolean> = stateOf(true), onClick:()->Unit){
        GuiComponent(modifier, alignment, window.button(text.value, onClick)).apply{
            if(text is MutState) text.listen { hwnd.name = it }
            if(enable is MutState) enable.listen { hwnd.enable(it) }
            hwnd.enable(enable.value)
        }.addToChild()
    }
    fun Edit(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(), text: State<String>,onEdit:(String)->Unit){
        GuiComponent(modifier, alignment, window.edit(text.value,onEdit)).apply {
            text.listen {
                val sel = hwnd.sel
                hwnd.name = it
                onEdit(it)
                hwnd.sel = sel
            }
        }.addToChild()
    }
    fun Custom(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(), block:()-> GuiWindow){
        GuiComponent(modifier, alignment, block().hwnd).addToChild()
    }
    fun Text(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(), text: State<String>){
        GuiComponent(modifier, alignment, window.text(text.value, alignment)).apply {
            if(text is MutState) text.listen { hwnd.name = it }
        }.addToChild()
    }
    fun ScrollableColumn(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(), block: ScrollableColumnScope.()->Unit){
        ScrollableColumnScope(modifier, alignment, window).apply(block).addToChild()
    }
    fun <T> List(list: MutStateList<T>, item: GuiScope.(T)-> Unit){
        class ListItem(val element:T,val scope: State.Scope,val child: GuiListChild)
        val fullList = GuiListChild(mutableListOf())
        children.add(fullList)
        val items = mutableListOf<ListItem>()
        list.listen(true,object:MutStateList.Listener<T>{
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
                item.child.destroyChild()
                item.scope.destroy()
                reLayout()
            }
            override fun onAnyChange() {}
        })
    }

    fun <T> By(state:State<T>,content:(T)-> Unit){
        val list = GuiListChild(mutableListOf())
        children.add(list)
        val scope = MutState.SimpleScope()
        _onDestroy += { scope.destroy() }
        state.listen(true) {
            scope.destroy()
            list.destroyChild()
            list.clear()
            list += remapChild {
                remapScope(scope){
                    content(it)
                }
            }
            reLayout()
        }
    }



    fun split(weight:FloatArray, min:IntArray,forced:IntArray,full:Int):IntArray = wrapExceptionName("split error"){
        require(weight.size == min.size) { "collection weight not match min" }
        if(min.sum() >= full) return min
        val indices = weight.indices
        val size = weight.size
        val results = IntArray(size){ forced[it].let { if(it == 0) Int.MIN_VALUE else it }  }
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
                warning("remain sum weight is zero")
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


    fun RECT.placeTB(modifier: Modifier, bound: RECT, align: Alignment) {
        val height = if(modifier.minH == 0) bound.height
        else modifier.run { minH + paddingH }
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


    fun RECT.placeLR(modifier: Modifier, bound: RECT, align: Alignment) {
        val width = if(modifier.minW == 0) bound.width
        else modifier.run { minW + paddingW }
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