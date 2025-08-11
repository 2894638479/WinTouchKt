package dsl

import logger.warning
import platform.windows.RECT
import wrapper.GuiWindow
import wrapper.height
import wrapper.width
import kotlin.math.max


@Gui
abstract class GuiScope(
    parent: GuiWindow?, name:String,
    modifier: Modifier,
    alignment: Alignment,
    style:Int = 0
): AbstractGuiComponent(modifier, alignment),MutState.Scope {
    override val _onDestroy = mutableListOf<()->Unit>()
    private val window = object : GuiWindow(name,
        if(parent == null) modifier.minW else 0,
        if(parent == null) modifier.minH else 0,
        style,parent
    ){
        val scope = this@GuiScope
        override fun onSize() = scope.onSize()
        override val scrollableHeight get() = scope.scrollableHeight
        override val scrollableWidth get() = scope.scrollableWidth
        override fun onDestroy(): Boolean {
            destroy()
            children.clear()
            onCommand.clear()
            return super.onDestroy()
        }
    }
    open val scrollableHeight get() = -1
    open val scrollableWidth get() = -1
    override val hwnd get() = window.hwnd
    protected val children = mutableListOf<AbstractGuiComponent>()
    protected val visibleChildren get() = children.filter { it.hwnd.visible }
    override val innerMinH get() = visibleChildren.maxOf { it.outerMinH }
    override val innerMinW get() = visibleChildren.maxOf { it.outerMinW }
    private var onChildAdd:(AbstractGuiComponent)->Unit = {}
    private fun AbstractGuiComponent.addToChild(){ children += this; onChildAdd(this) }
    private fun captureAddedChild(block: GuiScope.() -> Unit):List<AbstractGuiComponent>{
        val list = mutableListOf<AbstractGuiComponent>()
        onChildAdd = { list += it }
        block()
        onChildAdd = {}
        return list
    }
    abstract fun onSize()
    fun Box(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(), block: BoxScope.()->Unit){
        BoxScope(modifier, alignment, window).apply(block).addToChild()
    }
    fun Column(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(), block: ColumnScope.()->Unit){
        ColumnScope(modifier, alignment, window).apply(block).addToChild()
    }
    fun Row(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(), block: RowScope.()->Unit){
        RowScope(modifier, alignment, window).apply(block).addToChild()
    }
    fun Button(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(), text:State<String>, onClick:()->Unit){
        GuiComponent(modifier, alignment, window.button(text.value, onClick)).apply{
            if(text is MutState) text.listen { hwnd.name = it }
        }.addToChild()
    }
    fun Edit(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(), text: MutState<String>,onEdit:(String)->Unit){
        GuiComponent(modifier, alignment, window.edit(text.value) { text.value = it }).apply {
            text.listen { hwnd.name = it; onEdit(it) }
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
    fun VisibleIf(state: MutState<Boolean>, block: GuiScope.()->Unit){
        val list = captureAddedChild { block() }
        state.listen(true){
            if(it) list.forEach { it.hwnd.show() }
            else list.forEach { it.hwnd.hide() }
            onSize()
        }
    }
    fun ScrollableColumn(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(), block: ScrollableColumnScope.()->Unit){
        ScrollableColumnScope(modifier, alignment, window).apply(block).addToChild()
    }



    fun split(weight:FloatArray, min:IntArray,full:Int):IntArray{
        require(weight.size == min.size) { "collection weight not match min" }
        if(min.sum() >= full) return min
        val indices = weight.indices
        val size = weight.size
        val results = IntArray(size){ Int.MIN_VALUE }
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
                if(results[i] == Int.MIN_VALUE) results[i] = 0
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
                warning("column remain sum weight is zero")
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
        if (modifier.height == 0) {
            top = bound.top
            bottom = bound.bottom
        } else {
            val height = modifier.run { height + paddingH }
            if (align.bottom) {
                bottom = bound.bottom
                top = bottom - height
            } else if (align.middleY) {
                top = bound.top + (bound.height - height) / 2
                bottom = top + height
            } else if (align.top) {
                top = 0
                bottom = top + height
            }
        }
    }


    fun RECT.placeLR(modifier: Modifier, bound: RECT, align: Alignment) {
        if (modifier.width == 0) {
            left = bound.left
            right = bound.right
        } else {
            val width = modifier.run { width + paddingW }
            if (align.right) {
                right = bound.right
                left = right - width
            } else if (align.middleX) {
                left = bound.left + (bound.width - width) / 2
                right = left + width
            } else if (align.left) {
                left = 0
                right = width
            }
        }
    }
}