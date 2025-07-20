package dsl

import logger.warning
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
        val sumMinH = minH.sum()
        val heights = IntArray(vc.size){ Int.MIN_VALUE }
        if(sumMinH >= rect.height) {
            for(i in vc.indices){
                heights[i] = minH[i]
            }
        } else {
            var sumWeight = 0f
            var remain = 0
            fun calSumWeight(){
                sumWeight = 0f
                for (i in vc.indices){
                    if(heights[i] == Int.MIN_VALUE) sumWeight += vc[i].modifier.weight
                }
            }
            fun setOtherTo0(){
                for (i in vc.indices) {
                    if(heights[i] == Int.MIN_VALUE) heights[i] = 0
                }
            }
            fun calRemain(){
                remain = rect.height
                for (i in vc.indices){
                    if(heights[i] != Int.MIN_VALUE) remain -= heights[i]
                }
            }
            while(true){
                calSumWeight()
                if(sumWeight == 0f) {
                    setOtherTo0()
                    warning("column remain sum weight is zero")
                    break
                }
                calRemain()
                var added = false
                for (i in vc.indices){
                    if(heights[i] != Int.MIN_VALUE) continue
                    val thisH = (vc[i].modifier.weight / sumWeight * remain)
                    if(thisH <= minH[i]) {
                        heights[i] = minH[i]
                        added = true
                    }
                }
                if(!added) {
                    warning("heights $heights")
                    while (heights.find { it == Int.MIN_VALUE } != null){
                        calSumWeight()
                        if(sumWeight == 0f) {
                            setOtherTo0()
                            break
                        }
                        calRemain()
                        for (i in vc.indices) {
                            if(heights[i] != Int.MIN_VALUE) continue
                            val thisH = (vc[i].modifier.weight / sumWeight * remain)
                            warning("sumWeight = $sumWeight remain = $remain weight = ${vc[i].modifier.weight} h = $thisH")
                            heights[i] = thisH.toInt()
                            break
                        }
                    }
                    break
                }
            }
        }
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