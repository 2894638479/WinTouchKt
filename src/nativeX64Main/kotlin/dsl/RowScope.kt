package dsl

import wrapper.*

class RowScope(modifier: Modifier, alignment: Alignment, parent: GuiWindow?, name:String = "column"):
    GuiScope(parent,name,modifier, alignment) {
    fun Modifier.weight(value:Float) = apply { weight = value }
    override fun onSize() {
        var totalW = 0
        val rect = hwnd.rect
        rect.toOrigin()
        val vc = visibleChildren
        val minW = IntArray(vc.size){ vc[it].minW }
        val weight = FloatArray(vc.size){ vc[it].modifier.weight }
        val widths = split(weight,minW,rect.width)
        vc.forEachIndexed { i, it ->
            val modifier = it.modifier
            val align = it.alignment
            allocRECT {
                if(modifier.height == 0){
                    top = rect.top
                    bottom = rect.bottom
                } else {
                    if(align.bottom){
                        bottom = rect.bottom
                        top = bottom - modifier.fullHeight
                    } else if(align.middleY){
                        top = rect.top + (rect.height - modifier.fullHeight)/2
                        bottom = top + modifier.fullHeight
                    } else if(align.top){
                        top = 0
                        bottom = top + modifier.fullHeight
                    }
                }
                val offsetX = totalW
                left = offsetX
                right = left + widths[i]
                totalW += width
                it.hwnd.setRect(this)
            }
        }
    }
}