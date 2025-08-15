package geometry

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import wrapper.D2dBrush
import wrapper.D2dTarget
import wrapper.d2dDrawRound
import wrapper.d2dFillRound
import kotlin.math.pow

@Serializable
value class Round(
    val r:Float
): Shape {
    override fun containPointByRelativePos(x: Float, y: Float) = r>0 && x.pow(2) + y.pow(2) < r.pow(2)
    context(offset: Point)
    override fun d2dDraw(target: D2dTarget, brush: D2dBrush, width: Float) = target.d2dDrawRound(brush,offset.x,offset.y,r,r,width)
    context(offset: Point)
    override fun d2dFill(target: D2dTarget, brush: D2dBrush) = target.d2dFillRound(brush,offset.x,offset.y,r,r)
    override fun rescaled(scale: Float) = Round(r*scale)
    override val innerRect: Rect get() {
        val r = r * 0.70710677f
        return Rect(r,r)
    }
    override val outerRect: Rect get() = Rect(r,r)
    override fun padding(value: Float) = Round(r - value)
    override val isValid get() = r > 0
    override fun toString() = "Round${Json.encodeToString(this)}"
}