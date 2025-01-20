package button

import draw.paramBuffer
import draw.rescaleDpi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import kotlinx.cinterop.wcstr
import libs.Clib.d2dPopClip
import libs.Clib.d2dPushClip
import libs.Clib.d2dTargetHolder


interface Shape {
    val outlineWidth:Float
    fun containPoint(x: Float, y: Float):Boolean
    @OptIn(ExperimentalForeignApi::class)
    fun d2dDraw(target: CPointer<d2dTargetHolder>?, config: ButtonStyle)
    @OptIn(ExperimentalForeignApi::class)
    fun d2dFill(target: CPointer<d2dTargetHolder>?, config: ButtonStyle)
    @OptIn(ExperimentalForeignApi::class)
    fun d2dDrawText(target: CPointer<d2dTargetHolder>?, config: ButtonStyle, string: String){
        val par = paramBuffer.rect.apply {
            this.target = target
            val rect = innerRect
            l = rect.left
            t = rect.top
            r = rect.right
            b = rect.bottom
            brush = config.brushText
            rescaleDpi(target)
        }.ptr
        d2dPushClip(par,true)
        libs.Clib.d2dDrawText(par,config.font,string.wcstr)
        d2dPopClip(target)
    }
    fun rescaled(scale:Float):Shape
    fun offset(offset: Point):Shape
    val innerRect:Rect
    val outerRect:Rect
}