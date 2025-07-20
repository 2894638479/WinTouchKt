package dsl

import kotlinx.coroutines.flow.asFlow
import logger.warning
import wrapper.GuiWindow


@Gui
abstract class GuiScope(
    parent: GuiWindow?, name:String,
    modifier: Modifier,
    alignment: Alignment
): AbstractGuiComponent(modifier, alignment),MutState.Scope {
    override val _onDestroy = mutableListOf<()->Unit>()
    private val window = object : GuiWindow(name,
        if(parent == null) modifier.minW else 0,
        if(parent == null) modifier.minH else 0,parent){
        override fun onSize() = this@GuiScope.onSize()
        override fun onDestroy(): Boolean {
            destroy()
            children.clear()
            onCommand.clear()
            return super.onDestroy()
        }
    }
    override val hwnd get() = window.hwnd
    protected val children = mutableListOf<AbstractGuiComponent>()
    protected val visibleChildren get() = children.filter { it.hwnd.visible }
    override val minH get() = visibleChildren.maxOf { it.minH }
    override val minW get() = visibleChildren.maxOf { it.minW }
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
}