package geometry

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import wrapper.*
import kotlin.math.max
import kotlin.math.min


@Serializable(with = Rect.Serializer::class)
data class Rect(
    val left:Float,
    val top:Float,
    val right:Float,
    val bottom:Float
): Shape {
    class Mutable(
        var left:Float,
        var top:Float,
        var right:Float,
        var bottom:Float,
    ){
        operator fun plusAssign(other: Rect){
            left = min(left,other.left)
            top = min(top,other.top)
            right = max(right,other.right)
            bottom = max(bottom,other.bottom)
        }
        operator fun plusAssign(point: Point){
            left += point.x
            top += point.y
            right += point.x
            bottom += point.y
        }
        fun toRect(): Rect = Rect(left, top, right, bottom)
        override fun toString() = "Rect.Mutable${Json.encodeToString(toRect())}"
    }

    companion object {
        val empty = Rect(0f,0f,0f,0f)
        fun byPos(x: Float,y: Float,w: Float,h: Float): Rect{
            val w2 = w / 2
            val h2 = h / 2
            return Rect(x-w2,y-h2,x+w2,y+h2)
        }
    }

    val x get() = (left + right) / 2
    val y get() = (top + bottom) / 2
    val w get() = (right - left) / 2
    val h get() = (bottom - top) / 2
    fun toMutable(): Mutable = Mutable(left, top, right, bottom)
    override fun containPoint(x: Float, y: Float): Boolean {
        return x > left
                && y > top
                && x < right
                && y < bottom
    }
    override fun d2dDraw(target: D2dTarget, brush:D2dBrush,width:Float) = target.d2dDrawRect(brush,left,top,right,bottom,width)
    override fun d2dFill(target: D2dTarget, brush: D2dBrush) = target.d2dFillRect(brush,left,top,right,bottom)
    override fun rescaled(scale: Float) = Rect(
        left*scale,
        top*scale,
        right*scale,
        bottom*scale
    )

    override fun offset(offset: Point) = Rect(
        left + offset.x,
        top + offset.y,
        right + offset.x,
        bottom + offset.y
    )
    override fun padding(width:Float) = Rect(left + width, top + width, right - width, bottom - width)
    override val innerRect: Rect get() = this
    override val outerRect: Rect get() = this
    override val isValid get() = left < right && top < bottom

    object Serializer : SerializerWrapper<Rect,Serializer.Descriptor>("Rect",Descriptor){
        object Descriptor:SerializerWrapper.Descriptor<Rect>(){
            val x = "x" from {x}
            val y = "y" from {y}
            val w = "w" from {w}
            val h = "h" from {h}
        }
        override fun Descriptor.generate(): Rect = byPos(x.nonNull,y.nonNull,w.nonNull,h.nonNull)
    }
    override fun toString() = "Rect${Json.encodeToString(this)}"
}