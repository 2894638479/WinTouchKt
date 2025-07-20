package dsl

import wrapper.*

@DslMarker
annotation class Gui


abstract class AbstractGuiComponent(val modifier:Modifier, val alignment:Alignment){
    abstract val hwnd:Hwnd
    open val minW get() = modifier.layoutMinW
    open val minH get() = modifier.layoutMinH
}

class GuiComponent(modifier: Modifier, alignment: Alignment, override val hwnd: Hwnd):AbstractGuiComponent(modifier, alignment)

fun TopWindow(name: String, minW:Int, minH:Int, block: BoxScope.() -> Unit){
    BoxScope(Modifier().min(minW,minH),Alignment(),null,name).apply {
        _onDestroy += ::scheduleGC
        block()
        hwnd.show()
        hwnd.update()
        onSize()
    }
}


