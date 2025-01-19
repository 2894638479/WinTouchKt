package button

import draw.paramBuffer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import kotlinx.serialization.Serializable
import libs.Clib.d2dDrawRoundedRect
import libs.Clib.d2dFillRoundedRect
import libs.Clib.d2dTargetHolder

@Serializable
class RoundedRect(
    val left:Float,
    val top:Float,
    val right:Float,
    val bottom:Float,
    val r:Float
):Shape {
    override fun containPoint(x: Float, y: Float): Boolean {
        if(x > left && x < right && y > top && y < bottom) {
            if (x > left + r && x < right - r && y > top && y < bottom) {
                return true
            }
            if (x > left && x < right && y > top + r && y < bottom - r) {
                return true
            }
            fun inRound(x1: Float, y1: Float): Boolean {
                val dx = x - x1
                val dy = y - y1
                return dx * dx + dy * dy < r * r
            }
            return inRound(left + r, top + r)
                    || inRound(top + r, right - r)
                    || inRound(right - r, bottom - r)
                    || inRound(bottom - r,left + r)
        }
        return false
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun d2dDraw(target: CPointer<d2dTargetHolder>?, config: ButtonStyle) {
        d2dFillRoundedRect(paramBuffer.rect.apply {
            l = left
            t = top
            r = right
            b = bottom
            this.target = target
            brush = config.brush
        }.ptr,r,r)
        if(config.outlineWidth > 0f){
            d2dDrawRoundedRect(paramBuffer.rect.apply {
                brush = config.brushOutline
            }.ptr,r,r,config.outlineWidth)
        }
    }

    override val innerRect: Rect get() {
        val r1 = r * 0.29289323f
        return Rect(
            left + r1
            , top + r1
            , right - r1
            , bottom - r1
        )
    }
    override val outerRect: Rect
        get() = Rect(left, top, right, bottom)

}