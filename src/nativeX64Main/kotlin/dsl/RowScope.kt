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
            val minW = IntArray(vc.size) { vc[it].outerMinW.apply { warning(it.toString()) } }
            val weight = FloatArray(vc.size){ vc[it].modifier.run { if(width == 0) weight else 0f } }
            val staticW = vc.sumOf { if(it.modifier.width == 0) 0 else it.modifier.width + it.modifier.paddingW }
            val widths = split(weight,minW,rect.width - staticW).mapIndexed { i,it ->
                val modifier = vc[i].modifier
                if(modifier.width == 0) it else modifier.width + modifier.paddingW
            }
            vc.forEachIndexed { i, it ->
                val modifier = it.modifier
                val align = it.alignment
                allocRECT {
                    placeTB(modifier, rect, align)
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