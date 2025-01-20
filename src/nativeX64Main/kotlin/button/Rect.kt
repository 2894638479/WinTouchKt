package button

import draw.paramBuffer
import draw.rescaleDpi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import kotlinx.serialization.Serializable
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
    fun toRect():Rect = Rect(left, top, right, bottom,0f)
}

class Rect(
    val left:Float,
    val top:Float,
    val right:Float,
    val bottom:Float,
    override val outlineWidth: Float
):Shape{
    fun toMutableRect():MutableRect = MutableRect(left, top, right, bottom)
    override fun containPoint(x: Float, y: Float): Boolean {
        return x > left
                && y > top
                && x < right
                && y < bottom
    }
    @OptIn(ExperimentalForeignApi::class)
    override fun d2dDraw(target: CPointer<d2dTargetHolder>?, config: ButtonStyle) {
        if(outlineWidth <= 0f) return
        d2dDrawRect(paramBuffer.rect.apply {
            l = left
            t = top
            r = right
            b = bottom
            this.target = target
            brush = config.brushOutline
            rescaleDpi(target)
        }.ptr,outlineWidth)
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
            rescaleDpi(target)
        }.ptr)
    }

    override fun rescaled(scale: Float): Shape {
        return Rect(
            left*scale,
            top*scale,
            right*scale,
            bottom*scale,
            outlineWidth
        )
    }

    override fun offset(offset: Point): Shape {
        return Rect(
            left + offset.x,
            top + offset.y,
            right + offset.x,
            bottom + offset.y,
            outlineWidth
        )
    }

    override val innerRect: Rect get() {
        if(outlineWidth == 0f) return this
        val width = outlineWidth * 0.5f
        return Rect(
            left + width,
            top + width,
            right - width,
            bottom - width,
            0f
        )
    }
    override val outerRect: Rect get() = this
}