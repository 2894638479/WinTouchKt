package dsl

import error.wrapExceptionName
import geometry.Color
import logger.warning
import wrapper.*

class BoxScope(
    modifier: Modifier,
    alignment: Alignment,
    parent: WindowProcess?,
    name:String = "box",
    style:Int = 0,
    windowProcess:(WindowProcess)-> WindowProcess = {it}
):
    GuiScope(parent,name,modifier, alignment,style,windowProcess) {
    override fun onSize() = wrapExceptionName("BoxScope onSize") {
        visibleChildren.forEach {
            val modifier = it.modifier
            val align = it.alignment
            hwnd.useRect { rect ->
                rect.toOrigin()
                allocRECT {
                    placeLR(modifier,rect,align){it.outerMinW}
                    placeTB(modifier,rect,align){it.outerMinH}
                    padding(modifier)
                    it.hwnd.setRect(this)
                }
            }
        }
    }
}