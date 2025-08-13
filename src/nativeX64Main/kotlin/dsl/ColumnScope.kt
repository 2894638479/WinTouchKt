package dsl

import error.wrapExceptionName
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
            val weight = FloatArray(vc.size){ vc[it].modifier.run { if(height == 0) weight else 0f } }
            val staticH = vc.sumOf { if(it.modifier.height == 0) 0 else it.modifier.height + it.modifier.paddingH}
            val heights = split(weight,minH,rect.height - staticH).mapIndexed { i,it ->
                val modifier = vc[i].modifier
                if(modifier.height == 0) it else modifier.height + modifier.paddingH
            }
            vc.forEachIndexed { i, it ->
                val modifier = it.modifier
                val align = it.alignment
                allocRECT {
                    placeLR(modifier, rect, align)
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