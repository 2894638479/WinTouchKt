package dsl

import wrapper.GuiWindow

abstract class GuiScope(
    parent: GuiWindow?, name:String,
    modifier: Modifier,
    alignment: Alignment
): GuiComponent(modifier, alignment),MutState.Scope {
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
    fun VisibleIf(state: MutState<Boolean>, block: GuiScope.()->Unit){
        val list = captureAddedChild { block() }
        state.listen(true){
            if(it) list.forEach { it.hwnd.show() }
            else list.forEach { it.hwnd.hide() }
            onSize()
        }
    }


}