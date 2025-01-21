package button

import draw.paramBuffer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import kotlinx.serialization.Serializable
import libs.Clib.d2dDrawRound
import libs.Clib.d2dFillRound
import libs.Clib.d2dTargetHolder

@Serializable
class Round(
    val x:Float,
    val y:Float,
    val r:Float,
    override val outlineWidth: Float
):Shape{
    override fun containPoint(x: Float, y: Float): Boolean {
        val dx = (x - this.x)
        val dy = (y - this.y)
        return dx*dx + dy*dy < r*r
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun d2dDraw(target: CPointer<d2dTargetHolder>?, config: ButtonStyle) {
        if(outlineWidth <= 0) return
        d2dDrawRound(paramBuffer.round.apply {
            x = this@Round.x
            y = this@Round.y
            rx = r
            ry = r
            this.target = target
            brush = config.brushOutline
        }.ptr,outlineWidth)
    }
    @OptIn(ExperimentalForeignApi::class)
    override fun d2dFill(target: CPointer<d2dTargetHolder>?, config: ButtonStyle) {
        d2dFillRound(paramBuffer.round.apply {
            x = this@Round.x
            y = this@Round.y
            rx = r
            ry = r
            this.target = target
            brush = config.brush
        }.ptr)
    }

    override fun rescaled(scale: Float): Shape {
        return Round(
            x*scale,
            y*scale,
            r*scale,
            outlineWidth
        )
    }

    override fun offset(offset: Point): Shape {
        return Round(
            x + offset.x,
            y + offset.y,
            r,
            outlineWidth
        )
    }

    override val innerRect: Rect get() {
        val r = (r * 0.70710677f - outlineWidth * 0.5f).let{
            if(it > 0f) it else 0f
        }
        return Rect(
            left = x - r,
            top = y - r,
            right = x + r,
            bottom = y + r,
            0f
        )
    }

    override val outerRect: Rect get() {
        return Rect(
            left = x - r,
            top = y - r,
            right = x + r,
            bottom = y + r,
            0f
        )
    }
}