package button

import draw.paramBuffer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import kotlinx.cinterop.wcstr
import libs.Clib.d2dTargetHolder


interface Shape {
    fun containPoint(x: Float, y: Float):Boolean
    @OptIn(ExperimentalForeignApi::class)
    fun d2dDraw(target: CPointer<d2dTargetHolder>?, config: ButtonStyle)
    @OptIn(ExperimentalForeignApi::class)
    fun d2dDrawText(target: CPointer<d2dTargetHolder>?, config: ButtonStyle, string: String){
        libs.Clib.d2dDrawText(paramBuffer.rect.apply {
            this.target = target
            val rect = innerRect
            l = rect.left
            t = rect.top
            r = rect.right
            b = rect.bottom
            brush = config.brushText
        }.ptr,config.font,string.wcstr)
    }
    val innerRect:Rect
    val outerRect:Rect
}