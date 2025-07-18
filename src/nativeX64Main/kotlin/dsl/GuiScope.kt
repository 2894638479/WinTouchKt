package dsl

import wrapper.GuiWindow

abstract class GuiScope(
    parent: GuiWindow?, name:String,
    modifier: Modifier,
    alignment: Alignment
): GuiComponent(modifier, alignment),State.Scope {
    override val _onDestroy = mutableListOf<()->Unit>()
    private val window = object : GuiWindow(name,
        if(parent == null) modifier.width else 0,
        if(parent == null) modifier.height else 0,parent){
        override fun onSize() = this@GuiScope.onSize()
        override fun onDestroy(): Boolean {
            destroy()
            children.clear()
            onCommand.clear()
            return super.onDestroy()
        }
    }
    override val hwnd get() = window.hwnd
    protected val children = mutableListOf<GuiComponent>()
    private var onChildAdd:(GuiComponent)->Unit = {}
    private fun GuiComponent.addToChild(){ children += this; onChildAdd(this) }
    private fun captureAddedChild(block: GuiScope.() -> Unit):List<GuiComponent>{
        val list = mutableListOf<GuiComponent>()
        onChildAdd = { list += it }
        block()
        onChildAdd = {}
        return list
    }
    abstract fun onSize()
    fun Box(modifier: Modifier, alignment: Alignment, block: BoxScope.()->Unit){
        BoxScope(modifier, alignment, window).apply(block).addToChild()
    }
    fun Button(modifier: Modifier, alignment: Alignment, text:String, onClick:()->Unit){
        GuiHwnd(modifier, alignment, window.button(text, onClick)).addToChild()
    }
    fun Edit(modifier: Modifier, alignment: Alignment, text: String, onEdit:(String)->Unit){
        GuiHwnd(modifier, alignment, window.edit(text, onEdit)).addToChild()
    }
    fun Custom(modifier: Modifier, alignment: Alignment, block:()-> GuiWindow){
        GuiHwnd(modifier, alignment, block().hwnd).addToChild()
    }
    fun Text(modifier: Modifier, alignment: Alignment, text: String){
        GuiHwnd(modifier, alignment, window.text(text, alignment)).addToChild()
    }
    fun VisibleIf(state: State<Boolean>, block: GuiScope.()->Unit){
        val list = captureAddedChild { block() }
        state.listen(true){
            if(it) list.forEach { it.hwnd.show() }
            else list.forEach { it.hwnd.hide() }
            onSize()
        }
    }


    fun <P1,T> combine(p1: State<P1>, func:(P1)->T) = State(func(p1.value)).apply {
        p1.listen { value = func(it) }
    }
    fun <P1,P2,T> combine(p1: State<P1>, p2: State<P2>, func:(P1, P2)->T)
            = State(func(p1.value, p2.value)).apply {
        p1.listen { value = func(it,p2.value) }
        p2.listen { value = func(p1.value,it) }
    }
    fun <P1,P2,P3,T> combine(p1: State<P1>, p2: State<P2>, p3: State<P3>, func:(P1, P2, P3)->T)
            = State(func(p1.value, p2.value, p3.value)).apply {
        val calValue = { func(p1.value, p2.value, p3.value) }
        p1.listen { value = calValue() }
        p2.listen { value = calValue() }
        p3.listen { value = calValue() }
    }
    fun <P1,P2,P3,P4,T> combine(p1: State<P1>, p2: State<P2>, p3: State<P3>, p4: State<P4>, func:(P1, P2, P3, P4)->T)
            = State(func(p1.value, p2.value, p3.value, p4.value)).apply {
        val calValue = { func(p1.value, p2.value, p3.value, p4.value) }
        p1.listen { value = calValue() }
        p2.listen { value = calValue() }
        p3.listen { value = calValue() }
        p4.listen { value = calValue() }
    }
}