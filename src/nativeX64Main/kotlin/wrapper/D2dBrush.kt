package wrapper

import draw.Color
import error.brushCreateError
import kotlinx.cinterop.*
import kotlinx.cinterop.nativeHeap.alloc
import libs.Clib.d2dCreateSolidColorBrush
import libs.Clib.d2dFreeSolidColorBrush
import libs.Clib.d2dSolidColorBrushHolder

@OptIn(ExperimentalForeignApi::class)
value class D2dBrush(val value: CPointer<d2dSolidColorBrushHolder>){
    fun free() = d2dFreeSolidColorBrush(value)
    companion object {
        @OptIn(ExperimentalForeignApi::class)
        fun create(target: D2dTarget, key: Color) = memScoped {
            val brush = nativeHeap.alloc<CPointerVar<d2dSolidColorBrushHolder>>()
            d2dCreateSolidColorBrush(
                target.value,
                brush.ptr,
                key.r.toFloat() / 255f,
                key.g.toFloat() / 255f,
                key.b.toFloat() / 255f,
                1f
            )
            D2dBrush(brush.value ?: brushCreateError())
        }
    }
}