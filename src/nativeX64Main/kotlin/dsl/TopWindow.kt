package dsl

import wrapper.scheduleGC

class WindowManagerBuilder {
    val destroyOnChange = mutableListOf<State<*>>()
    val destroyOnFalse = mutableListOf<State<Boolean>>()
}

interface WindowManager {
    operator fun plusAssign(window: GuiScope)
}

val topWindows = object : WindowManager {
    val list = mutableListOf<GuiScope>()
    override fun plusAssign(window: GuiScope) {
        list += window
    }
}

context(guiScope: GuiScope)
fun scopeWindows(block: WindowManagerBuilder.()->Unit) = object : WindowManager {
    val list = mutableListOf<GuiScope>()
    override fun plusAssign(window: GuiScope) {
        list += window
    }
    private fun destroy(){
        list.forEach { it.destroyHwnd() }
        list.clear()
    }
    init {
        val builder = WindowManagerBuilder().apply(block)
        guiScope._onDestroy += ::destroy
        builder.destroyOnChange.forEach {
            it.listen {
                destroy()
            }
        }
        builder.destroyOnFalse.forEach {
            it.listen {
                if(!it) destroy()
            }
        }
    }
}

context(wm: WindowManager)
fun Window(name: String, modifier: Modifier, block: BoxScope.() -> Unit): GuiScope{
    val scope = BoxScope(modifier,A,null,name).apply {
        _onDestroy += ::scheduleGC
        block()
        hwnd.show()
        hwnd.update()
        onSize()
    }
    wm += scope
    return scope
}