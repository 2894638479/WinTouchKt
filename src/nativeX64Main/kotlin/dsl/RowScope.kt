package dsl

import platform.windows.RECT
import wrapper.*

class RowScope(modifier: Modifier, alignment: Alignment, parent: GuiWindow?, name:String = "column"):
    GuiScope(parent,name,modifier, alignment) {
    fun Modifier.weight(value:Float) = apply { weight = value }
    override fun onSize() {
        var totalW = 0
        val rect = hwnd.rect.apply { toOrigin() }
        val vc = visibleChildren
        val minW = IntArray(vc.size){ vc[it].outerMinW }
        val weight = FloatArray(vc.size){ vc[it].modifier.weight }
        val widths = split(weight,minW,rect.width)
        vc.forEachIndexed { i, it ->
            val modifier = it.modifier
            val align = it.alignment
            allocRECT {
                placeTB(modifier, rect, align)
                val offsetX = totalW
                left = offsetX
                right = left + widths[i]
                totalW += width
                it.hwnd.rect = this
            }
        }
    }
}