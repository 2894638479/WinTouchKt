package dsl

import wrapper.*


abstract class AbstractGuiComponent(val modifier:Modifier, val alignment:Alignment){
    abstract val hwnd:Hwnd
}

class GuiComponent(modifier: Modifier, alignment: Alignment, override val hwnd: Hwnd):AbstractGuiComponent(modifier, alignment)

fun TopWindow(name: String, minW:Int, minH:Int, block: BoxScope.() -> Unit){
    BoxScope(Modifier().size(minW,minH),Alignment(),null,name).apply {
        _onDestroy += ::scheduleGC
        block()
        hwnd.show()
        hwnd.update()
        onSize()
    }
}


