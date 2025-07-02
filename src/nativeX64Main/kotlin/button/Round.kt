package button

import kotlinx.serialization.Serializable
import wrapper.D2dBrush
import wrapper.D2dTarget
import wrapper.d2dDrawRound
import wrapper.d2dFillRound
import kotlin.math.pow
import kotlin.math.withSign

@Serializable
class Round(
    val x:Float,
    val y:Float,
    val r:Float
):Shape{
    override fun containPoint(x: Float, y: Float) = (x - this.x).pow(2) + (y - this.y).pow(2) < r.pow(2).withSign(r)
    override fun d2dDraw(target: D2dTarget, brush: D2dBrush, width: Float) = target.d2dDrawRound(brush,x,y,r,r,width)
    override fun d2dFill(target: D2dTarget, brush: D2dBrush) = target.d2dFillRound(brush,x,y,r,r)
    override fun rescaled(scale: Float) = Round(x*scale, y*scale, r*scale)
    override fun offset(offset: Point) = Round(x + offset.x, y + offset.y, r)
    override val innerRect: Rect get() {
        val r = r * 0.70710677f
        return Rect(x - r,y - r,x + r,y + r)
    }
    override val outerRect: Rect get() = Rect( x - r,y - r, x + r,y + r)
    override fun padding(width: Float) = Round(x, y, r - width)
    override val isValid get() = r > 0
}