package button

import draw.paramBuffer
import draw.rescaleDpi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import kotlinx.serialization.Serializable
import libs.Clib.*

@Serializable
class Round(
    val x:Float,
    val y:Float,
    val r:Float,
):Shape{
    override fun containPoint(x: Float, y: Float): Boolean {
        val dx = (x - this.x)
        val dy = (y - this.y)
        return dx*dx + dy*dy < r*r
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun d2dDraw(target: CPointer<d2dTargetHolder>?, config: ButtonStyle) {
        d2dFillRound(paramBuffer.round.apply {
            x = this@Round.x
            y = this@Round.y
            rx = r
            ry = r
            this.target = target
            brush = config.brush
        }.rescaleDpi(target).ptr)
        if(config.outlineWidth > 0f){
            d2dDrawRound(paramBuffer.round.apply {
                brush = config.brushOutline
            }.ptr,config.outlineWidth)
        }
    }

    override val innerRect: Rect get() {
        val r = r * 0.70710677f
        return Rect(
            left = x - r,
            top = y - r,
            right = x + r,
            bottom = y + r
        )
    }

    override val outerRect: Rect get() {
        return Rect(
            left = x - r,
            top = y - r,
            right = x + r,
            bottom = y + r
        )
    }
}