package dsl

import error.wrapExceptionName
import logger.warning
import wrapper.*

class RowScope(modifier: Modifier, alignment: Alignment, parent: GuiWindow?, name:String = "row"):
    GuiScope(parent,name,modifier, alignment) {
    fun Modifier.weight(value:Float) = apply { weight = value }
    override fun onSize() = wrapExceptionName("RowScope onSize") {
        var totalW = 0
        hwnd.useRect { rect ->
            rect.toOrigin()
            val vc = visibleChildren
            val minW = IntArray(vc.size) { vc[it].outerMinW }
            val forcedW = IntArray(vc.size) { vc[it].modifier.run { if(width == 0) 0 else width + paddingW } }
            val weight = FloatArray(vc.size){ vc[it].modifier.weight }
            val widths = split(weight,minW,forcedW,rect.width)
            vc.forEachIndexed { i, it ->
                val modifier = it.modifier
                val align = it.alignment
                allocRECT {
                    placeTB(modifier, rect, align){it.outerMinH}
                    val offsetX = totalW
                    left = offsetX
                    right = left + widths[i]
                    totalW += width
                    padding(modifier)
                    it.hwnd.setRect(this)
                }
            }
        }
    }
}