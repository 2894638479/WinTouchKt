package dsl

import wrapper.*
import kotlin.math.max

@DslMarker
annotation class Gui


abstract class AbstractGuiComponent(val modifier:Modifier, val alignment:Alignment){
    abstract val hwnd:Hwnd
    open val innerMinW get() = modifier.minW
    open val innerMinH get() = modifier.minH
    val outerMinW get() = max(modifier.minW,innerMinW) + modifier.paddingW
    val outerMinH get() = max(modifier.minH,innerMinH) + modifier.paddingH
}

class GuiComponent(modifier: Modifier, alignment: Alignment, override val hwnd: Hwnd):AbstractGuiComponent(modifier, alignment)

fun TopWindow(name: String, minW:Int, minH:Int, block: BoxScope.() -> Unit):Hwnd{
    return BoxScope(Modifier().min(minW,minH),Alignment(),null,name).apply {
        _onDestroy += ::scheduleGC
        block()
        hwnd.show()
        hwnd.update()
        onSize()
    }.hwnd
}


