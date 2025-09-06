package dsl

import wrapper.*
import kotlin.math.max

@DslMarker
annotation class Gui

interface GuiChild{
    fun addTo(list: MutableList<AbstractGuiComponent>)
    fun destroyHwnd()
}
value class GuiListChild(val list : MutableList<GuiChild>) : GuiChild, MutableList<GuiChild> by list{
    override fun addTo(list: MutableList<AbstractGuiComponent>) = forEach { it.addTo(list) }
    override fun destroyHwnd() = forEach { it.destroyHwnd() }
}
abstract class AbstractGuiComponent(val modifier:Modifier, val alignment:Alignment): GuiChild{
    abstract val hwnd:Hwnd
    open val innerMinW get() = modifier.minW
    open val innerMinH get() = modifier.minH
    val outerMinW get() = max(modifier.minW,innerMinW) + modifier.paddingW
    val outerMinH get() = max(modifier.minH,innerMinH) + modifier.paddingH
    final override fun addTo(list: MutableList<AbstractGuiComponent>) { list += this }
    final override fun destroyHwnd() = hwnd.destroy()
}

class GuiComponent(modifier: Modifier, alignment: Alignment, override val hwnd: Hwnd):AbstractGuiComponent(modifier, alignment)


