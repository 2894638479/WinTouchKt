package button

import draw.paramBuffer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import libs.Clib.*
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
    @OptIn(ExperimentalForeignApi::class)
    override fun d2dDraw(target: CPointer<d2dTargetHolder>?, config: ButtonStyle) {
        val width = config.outlineWidth ?: return
        if(width<= 0f) return
        d2dDrawRect(paramBuffer.rect.apply {
            l = left
            t = top
            r = right
            b = bottom
            this.target = target
            brush = config.brushOutline
        }.ptr,width)
    }
    @OptIn(ExperimentalForeignApi::class)
    override fun d2dFill(target: CPointer<d2dTargetHolder>?, config: ButtonStyle) {
        d2dFillRect(paramBuffer.rect.apply {
            l = left
            t = top
            r = right
            b = bottom
            this.target = target
            brush = config.brush
        }.ptr)
    }

    override fun rescaled(scale: Float): Shape {
        return Rect(
            left*scale,
            top*scale,
            right*scale,
            bottom*scale
        )
    }

    override fun offset(offset: Point): Shape {
        return Rect(
            left + offset.x,
            top + offset.y,
            right + offset.x,
            bottom + offset.y
        )
    }
    fun padding(value:Float) = Rect(left + value, top + value, right - value, bottom - value)

    override val innerRect: Rect get() = this
    override val outerRect: Rect get() = this
}