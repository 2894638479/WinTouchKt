package dsl

import wrapper.scheduleGC

class WindowManagerBuilder {
    val destroyOnChange = mutableListOf<State<*>>()
    val destroyOnFalse = mutableListOf<State<Boolean>>()
}

interface WindowManager {
    context(parentScope: GuiScope)
    fun add(window: GuiScope)
}

context(guiScope: GuiScope)
fun scopeWindows(block: WindowManagerBuilder.()->Unit) = object : WindowManager {
    val list = mutableListOf<GuiScope>()
    context(parentScope: GuiScope)
    override fun add(window: GuiScope) {
        list += window
        parentScope._onDestroy += {
            if(list.remove(window)) {
                window.destroyHwnd()
            }
        }
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
fun GuiScope.Window(name: String, modifier: Modifier, block: BoxScope.() -> Unit)
= TopWindow(name,modifier,block).also { wm.add(it) }

fun TopWindow(name: String, modifier: Modifier, block: BoxScope.() -> Unit): GuiScope{
    val window = BoxScope(modifier,A,null,name).apply {
        _onDestroy += ::scheduleGC
        block()
        hwnd.show()
        hwnd.update()
        onSize()
    }
    return window
}