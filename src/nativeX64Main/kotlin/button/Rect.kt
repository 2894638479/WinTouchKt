package button

import wrapper.D2dBrush
import wrapper.D2dTarget
import wrapper.d2dDrawRect
import wrapper.d2dFillRect
import kotlin.math.max
import kotlin.math.min

class MutableRect(
    var left:Float,
    var top:Float,
    var right:Float,
    var bottom:Float,
){
    operator fun plusAssign(other:Rect){
        left = min(left,other.left)
        top = min(top,other.top)
        right = max(right,other.right)
        bottom = max(bottom,other.bottom)
    }
    operator fun plusAssign(point:Point){
        left += point.x
        top += point.y
        right += point.x
        bottom += point.y
    }
    fun toRect():Rect = Rect(left, top, right, bottom)
}

class Rect(
    val left:Float,
    val top:Float,
    val right:Float,
    val bottom:Float
):Shape{
    val x get() = (left + right) / 2
    val y get() = (top + bottom) / 2
    val w get() = (right - left) / 2
    val h get() = (bottom - top) / 2
    fun toMutableRect():MutableRect = MutableRect(left, top, right, bottom)
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
}