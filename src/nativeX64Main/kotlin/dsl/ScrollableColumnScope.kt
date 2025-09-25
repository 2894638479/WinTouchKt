package dsl

import error.wrapExceptionName
import platform.windows.GetSystemMetrics
import platform.windows.SM_CXVSCROLL
import platform.windows.WS_VSCROLL
import wrapper.GuiWindow
import wrapper.allocRECT
import wrapper.toOrigin

class ScrollableColumnScope(modifier: Modifier, alignment: Alignment, parent: GuiWindow?, name:String = "column"):
    GuiScope(parent,name,modifier, alignment, style = WS_VSCROLL) {
    override var scrollableHeight = 0
    override val innerMinW get() = super.innerMinW + GetSystemMetrics(SM_CXVSCROLL)
    override val innerMinH get() = 0
    fun Modifier.weight(value:Float) = apply { weight = value }
    override fun onSize() = wrapExceptionName("ScrollableColumn onSize") {
        var h = 0
        visibleChildren.forEach {
            hwnd.useRect { rect ->
                rect.toOrigin()
                allocRECT {
                    placeLR(it.modifier, rect, it.alignment){it.outerMinW}
                    h += it.modifier.paddingTop
                    top = h
                    h += it.innerMinH
                    bottom = h
                    h += it.modifier.paddingBottom
                    left += it.modifier.paddingLeft
                    right -= it.modifier.paddingRight
                    it.hwnd.setRect(this)
                }
            }
        }
        scrollableHeight = h
    }
}