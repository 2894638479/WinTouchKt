package dsl

import error.wrapExceptionName
import logger.warning
import wrapper.*

class BoxScope(modifier: Modifier, alignment: Alignment, parent: GuiWindow?, name:String = "box"):
    GuiScope(parent,name,modifier, alignment) {
    override fun onSize() = wrapExceptionName("BoxScope onSize") {
        visibleChildren.forEach {
            val modifier = it.modifier
            val align = it.alignment
            hwnd.useRect { rect ->
                rect.toOrigin()
                allocRECT {
                    placeLR(modifier,rect,align)
                    placeTB(modifier,rect,align)
                    padding(modifier)
                    it.hwnd.setRect(this)
                }
            }
        }
    }
}