package dsl

import error.wrapExceptionName
import logger.info
import logger.warning
import wrapper.*

class ColumnScope(modifier: Modifier, alignment: Alignment, parent: GuiWindow?, name:String = "column"):
    GuiScope(parent,name,modifier, alignment) {
    fun Modifier.weight(value:Float) = apply { weight = value }
    override val innerMinH get() = visibleChildren.sumOf { it.outerMinH }
    override fun onSize() = wrapExceptionName("ColumnScope onSize") {
        var totalH = 0
        hwnd.useRect { rect ->
            rect.toOrigin()
            val vc = visibleChildren
            val minH = IntArray(vc.size){ vc[it].outerMinH }
            val staticH = IntArray(vc.size){ vc[it].modifier.run { if(height == 0) 0 else height + paddingH }}
            val weight = FloatArray(vc.size){ vc[it].modifier.weight }
            val heights = split(weight,minH,staticH,rect.height)
            vc.forEachIndexed { i, it ->
                val modifier = it.modifier
                val align = it.alignment
                allocRECT {
                    placeLR(modifier, rect, align){it.outerMinW}
                    val offsetY = totalH
                    top = offsetY
                    bottom = top + heights[i]
                    totalH += height
                    padding(modifier)
                    it.hwnd.setRect(this)
                }
            }
        }
    }
}