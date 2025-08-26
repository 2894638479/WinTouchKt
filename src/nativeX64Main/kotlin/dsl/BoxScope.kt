package dsl

import error.wrapExceptionName
import geometry.Color
import logger.warning
import wrapper.*

class BoxScope(modifier: Modifier, alignment: Alignment, parent: GuiWindow?,
               name:String = "box",color:State<Color?> = stateNull()):
    GuiScope(parent,name,modifier, alignment,color) {
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