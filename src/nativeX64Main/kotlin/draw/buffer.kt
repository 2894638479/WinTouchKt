package draw

import kotlinx.cinterop.*
import libs.Clib.*

@OptIn(ExperimentalForeignApi::class)
val paramBuffer:d2dDrawParaBuffer = nativeHeap.alloc()

@OptIn(ExperimentalForeignApi::class)
fun d2dDrawRectPara.rescaleDpi(target: CValuesRef<d2dTargetHolder>?): d2dDrawRectPara{
    d2dGetDpi(target).useContents {
        val scaleX = 96f/x
        val scaleY = 96f/y
        l *= scaleX
        t *= scaleY
        r *= scaleX
        b *= scaleY
    }
    return this
}

@OptIn(ExperimentalForeignApi::class)
fun d2dDrawRoundPara.rescaleDpi(target: CValuesRef<d2dTargetHolder>?): d2dDrawRoundPara{
    d2dGetDpi(target).useContents {
        val scaleX = 96f/x
        val scaleY = 96f/y
        this@rescaleDpi.x *= scaleX
        this@rescaleDpi.y *= scaleY
        this@rescaleDpi.rx *= scaleX
        this@rescaleDpi.ry *= scaleY
    }
    return this
}