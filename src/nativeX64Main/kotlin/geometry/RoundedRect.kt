package geometry

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import wrapper.*
import kotlin.math.max

@Serializable
data class RoundedRect(
    val width: Float,
    val height: Float,
    val r:Float
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
        val left = -width/2
        val right = width/2
        val top = -height/2
        val bottom = height/2
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
    context(offset: Point)
    override fun d2dDraw(target: D2dTarget, brush: D2dBrush, width: Float) = target.d2dDrawRoundedRect(brush,left,top,right,bottom,r,r,width)
    context(offset: Point)
    override fun d2dFill(target: D2dTarget, brush: D2dBrush) = target.d2dFillRoundedRect(brush,left,top,right,bottom,r,r)
    override fun rescaled(scale: Float) = RoundedRect(width*scale,height*scale,r*scale)
    override fun padding(value: Float) = RoundedRect(width-value*2,height-value*2, max(r - value,0f))
    override val innerRect: Rect get() {
        val r1 = r * 0.29289323f * 2
        return Rect(width-r1,height-r1)
    }
    override val outerRect: Rect get() = Rect(width,height)
    override val isValid get() = width > 0 && height > 0 && r > 0
    override fun toString() = "RoundedRect${Json.encodeToString(this)}"
}