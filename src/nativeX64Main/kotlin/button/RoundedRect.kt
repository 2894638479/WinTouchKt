package button

import draw.paramBuffer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import kotlinx.cinterop.useContents
import libs.Clib.d2dDrawRoundedRect
import libs.Clib.d2dFillRoundedRect
import libs.Clib.d2dGetDpi
import libs.Clib.d2dTargetHolder

class RoundedRect(
    val left:Float,
    val top:Float,
    val right:Float,
    val bottom:Float,
    val r:Float
):Shape {
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

    @OptIn(ExperimentalForeignApi::class)
    override fun d2dDraw(target: CPointer<d2dTargetHolder>?, config: ButtonStyle) {
        val width = config.outlineWidth ?: return
        if(width<= 0f) return
        val R = rescaledR(target)
        d2dDrawRoundedRect(paramBuffer.rect.apply {
            l = left
            t = top
            r = right
            b = bottom
            this.target = target
            brush = config.brushOutline
        }.ptr,R.first,R.second,width)
    }
    @OptIn(ExperimentalForeignApi::class)
    override fun d2dFill(target: CPointer<d2dTargetHolder>?, config: ButtonStyle) {
        val R = rescaledR(target)
        d2dFillRoundedRect(paramBuffer.rect.apply {
            l = left
            t = top
            r = right
            b = bottom
            this.target = target
            brush = config.brush
        }.ptr,R.first,R.second)
    }

    override fun rescaled(scale: Float): Shape {
        return RoundedRect(
            left*scale,
            top*scale,
            right*scale,
            bottom*scale,
            r*scale
        )
    }

    override fun offset(offset: Point): Shape {
        return RoundedRect(
            left + offset.x,
            top + offset.y,
            right + offset.x,
            bottom + offset.y,
            r
        )
    }
    override val innerRect: Rect get() {
        val r1 = r * 0.29289323f
        return Rect(
            left + r1,
            top + r1,
            right - r1,
            bottom - r1
        )
    }
    override val outerRect: Rect
        get() = Rect(left, top, right, bottom)
    @OptIn(ExperimentalForeignApi::class)
    fun rescaledR(target: CPointer<d2dTargetHolder>?):Pair<Float,Float>{
        d2dGetDpi(target).useContents {
            val scaleX = 96f/x
            val scaleY = 96f/y
            return r*scaleX to r*scaleY
        }
    }
}