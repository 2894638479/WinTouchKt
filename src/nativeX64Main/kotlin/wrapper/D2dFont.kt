package wrapper

import draw.Font
import error.fontCreateError
import kotlinx.cinterop.*
import kotlinx.cinterop.nativeHeap.alloc
import libs.Clib.FONT_STRETCH_MEDIUM
import libs.Clib.d2dCreateTextFormat
import libs.Clib.d2dFreeTextFormat
import libs.Clib.d2dTextFormatHolder

@OptIn(ExperimentalForeignApi::class)
value class D2dFont(val value: CPointer<d2dTextFormatHolder>){
    fun free() = d2dFreeTextFormat(value)
    companion object {
        @OptIn(ExperimentalForeignApi::class)
        fun create(writeFactory: D2dWriteFactory, font: Font) = memScoped {
            val fontHolder = nativeHeap.alloc<CPointerVar<d2dTextFormatHolder>>()
            d2dCreateTextFormat(
                writeFactory.value,
                fontHolder.ptr,
                font.family.wcstr,
                font.size,
                font.weight,
                font.style,
                FONT_STRETCH_MEDIUM
            ).let { if (it != 0) fontCreateError(font.family) }
            D2dFont(fontHolder.value ?: fontCreateError(font.family))
        }
    }
}