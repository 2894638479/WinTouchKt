package wrapper

import draw.Color
import draw.Font
import error.brushCreateError
import error.fontCreateError
import kotlinx.cinterop.*
import libs.Clib.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference

@OptIn(ExperimentalForeignApi::class)
fun D2dWriteFactory.createFont(font: Font) = memScoped {
    val fontHolder = nativeHeap.alloc<CPointerVar<d2dTextFormatHolder>>()
    d2dCreateTextFormat(
        value,
        fontHolder.ptr,
        font.family.wcstr,
        font.size,
        font.weight,
        font.style,
        FONT_STRETCH_MEDIUM
    ).let { if(it != 0) fontCreateError(font.family) }
    D2dFont(fontHolder.value ?: fontCreateError(font.family))
}

@OptIn(ExperimentalForeignApi::class)
fun D2dTarget.createBrush(key: Color) = memScoped {
    val brush = nativeHeap.alloc<CPointerVar<d2dSolidColorBrushHolder>>()
    d2dCreateSolidColorBrush(
        value,
        brush.ptr,
        key.r.toFloat() / 255f,
        key.g.toFloat() / 255f,
        key.b.toFloat() / 255f,
        1f
    )
    D2dBrush(brush.value ?: brushCreateError())
}