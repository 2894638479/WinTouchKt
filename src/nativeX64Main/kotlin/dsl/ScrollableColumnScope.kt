package dsl

import platform.windows.GetSystemMetrics
import platform.windows.SM_CXVSCROLL
import platform.windows.WS_VSCROLL
import wrapper.GuiWindow
import wrapper.allocRECT
import wrapper.toOrigin

class ScrollableColumnScope(modifier: Modifier, alignment: Alignment, parent: GuiWindow?, name:String = "column"):
    GuiScope(parent,name,modifier, alignment, WS_VSCROLL) {
    override var scrollableHeight = 0
    override val innerMinW get() = super.innerMinW + GetSystemMetrics(SM_CXVSCROLL)
    fun Modifier.weight(value:Float) = apply { weight = value }
    override fun onSize() {
        var h = 0
        visibleChildren.forEach {
            allocRECT {
                val rect = hwnd.rect.apply { toOrigin() }
                placeLR(it.modifier,rect,it.alignment)
                h += it.modifier.paddingTop
                top = h
                h += it.innerMinH
                bottom = h
                h += it.modifier.paddingBottom
                it.hwnd.rect = this
            }
        }
        scrollableHeight = h
    }
}