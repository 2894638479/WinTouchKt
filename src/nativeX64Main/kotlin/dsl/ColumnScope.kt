package dsl

import wrapper.*

class ColumnScope(modifier: Modifier, alignment: Alignment, parent: GuiWindow?, name:String = "column"):
    GuiScope(parent,name,modifier, alignment) {
    fun Modifier.weight(value:Float) = apply { weight = value }
    override fun onSize() {
        var totalH = 0
        val rect = hwnd.rect
        rect.toOrigin()
        val vc = visibleChildren
        val minH = IntArray(vc.size){ vc[it].minH }
        val weight = FloatArray(vc.size){ vc[it].modifier.weight }
        val heights = split(weight,minH,rect.height)
        vc.forEachIndexed { i, it ->
            val modifier = it.modifier
            val align = it.alignment
            allocRECT {
                if(modifier.width == 0){
                    left = rect.left
                    right = rect.right
                } else {
                    if(align.right){
                        right = rect.right
                        left = right - modifier.fullWidth
                    } else if(align.middleX){
                        left = rect.left + (rect.width - modifier.fullWidth)/2
                        right = left + modifier.fullWidth
                    } else if(align.left){
                        left = 0
                        right = modifier.fullWidth
                    }
                }
                val offsetY = totalH
                top = offsetY
                bottom = top + heights[i]
                totalH += height
                it.hwnd.setRect(this)
            }
        }
    }
}