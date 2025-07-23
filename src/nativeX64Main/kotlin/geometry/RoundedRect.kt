package geometry

import geometry.Rect.Serializer.Descriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import wrapper.*
import kotlin.math.max

@Serializable(with = RoundedRect.Serializer::class)
class RoundedRect(
    val left:Float,
    val top:Float,
    val right:Float,
    val bottom:Float,
    val r:Float
): Shape {
    val x get() = (left + right) / 2
    val y get() = (top + bottom) / 2
    val w get() = (right - left) / 2
    val h get() = (bottom - top) / 2
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
    override fun d2dDraw(target: D2dTarget, brush: D2dBrush, width: Float) = target.d2dDrawRoundedRect(brush,left,top,right,bottom,r,r,width)
    override fun d2dFill(target: D2dTarget, brush: D2dBrush) = target.d2dFillRoundedRect(brush,left,top,right,bottom,r,r)
    override fun rescaled(scale: Float) = RoundedRect(left*scale, top*scale, right*scale, bottom*scale, r*scale)
    override fun offset(offset: Point) = RoundedRect(left + offset.x, top + offset.y, right + offset.x, bottom + offset.y,r)
    override fun padding(width: Float) = RoundedRect(left + width, top + width, right - width, bottom - width, max(r - width,0f))
    override val innerRect: Rect
        get() {
        val r1 = r * 0.29289323f
        return Rect(left + r1, top + r1, right - r1, bottom - r1)
    }
    override val outerRect: Rect get() = Rect(left, top, right, bottom)
    override val isValid get() = left < right && top < bottom

    object Serializer:SerializerWrapper<RoundedRect,Serializer.Descriptor>("RoundedRect",Descriptor){
        object Descriptor:SerializerWrapper.Descriptor<RoundedRect>(){
            val x = "x" from {x}
            val y = "y" from {y}
            val w = "w" from {w}
            val h = "h" from {h}
            val r = "r" from {r}
        }
        override fun Descriptor.generate(): RoundedRect {
            val x = x.nonNull
            val y = y.nonNull
            val w2 = w.nonNull / 2
            val h2 = h.nonNull / 2
            return RoundedRect(x - w2,y - h2,x + w2,y + h2,r.nonNull)
        }
    }
    override fun toString() = "RoundedRect${Json.encodeToString(this)}"
}