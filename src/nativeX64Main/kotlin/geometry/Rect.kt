package geometry

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import wrapper.*


@Serializable
data class Rect(
    val width: Float,
    val height: Float
): Shape {
    context(point:Point)
    inline val top get() = point.y - height/2
    context(point:Point)
    inline val bottom get() = point.y + height/2
    context(point:Point)
    inline val left get() = point.x - width/2
    context(point:Point)
    inline val right get() = point.x + width/2
    override fun containPointByRelativePos(x: Float, y: Float): Boolean {
        val w2 = width/2
        val h2 = height/2
        return x > -w2 && x < w2 && y > -h2 && y < h2
    }
    context(offset: Point)
    override fun d2dDraw(target: D2dTarget, brush:D2dBrush,width:Float) = target.d2dDrawRect(brush,left,top,right,bottom,width)
    context(offset: Point)
    override fun d2dFill(target: D2dTarget, brush: D2dBrush) = target.d2dFillRect(brush,left,top,right,bottom)
    override fun rescaled(scale: Float) = Rect(width*scale, height*scale)
    override fun padding(value:Float) = Rect(width - 2*value,height - 2*value)
    override val innerRect: Rect get() = this
    override val outerRect: Rect get() = this
    override val isValid get() = width > 0 && height > 0
    override fun toString() = "Rect${Json.encodeToString(this)}"
}