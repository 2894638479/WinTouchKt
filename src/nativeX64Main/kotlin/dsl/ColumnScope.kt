package dsl

import platform.windows.RECT
import wrapper.*

class ColumnScope(modifier: Modifier, alignment: Alignment, parent: GuiWindow?, name:String = "column"):
    GuiScope(parent,name,modifier, alignment) {
    fun Modifier.weight(value:Float) = apply { weight = value }
    override val innerMinH get() = visibleChildren.sumOf { it.outerMinH }
    override fun onSize() {
        var totalH = 0
        val rect = hwnd.rect.apply { toOrigin() }
        val vc = visibleChildren
        val minH = IntArray(vc.size){ vc[it].outerMinH }
        val weight = FloatArray(vc.size){ vc[it].modifier.weight }
        val heights = split(weight,minH,rect.height)
        vc.forEachIndexed { i, it ->
            val modifier = it.modifier
            val align = it.alignment
            allocRECT {
                placeLR(modifier, rect, align)
                val offsetY = totalH
                top = offsetY
                bottom = top + heights[i]
                totalH += height
                it.hwnd.rect = this
            }
        }
    }
}