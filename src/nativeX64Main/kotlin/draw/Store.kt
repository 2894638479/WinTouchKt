package draw

import error.brushCreateError
import error.fontCreateError
import error.nullPtrError
import kotlinx.cinterop.*
import libs.Clib.*

@OptIn(ExperimentalForeignApi::class)
object Store {
    private val brushes = mutableMapOf<Color, CPointer<d2dSolidColorBrushHolder>>()
    private val fonts = mutableMapOf<Font, CPointer<d2dTextFormatHolder>>()
    var writeFactory: CValuesRef<d2dWriteFactoryHolder>? = null
    var target: CValuesRef<d2dTargetHolder>? = null
    fun font(key:Font) = fonts[key] ?: memScoped {
        val font = nativeHeap.alloc<CPointerVar<d2dTextFormatHolder>>()
        val family = key.family ?: ""
        d2dCreateTextFormat(
            writeFactory ?: nullPtrError(),
            font.ptr,
            family.wcstr,
            key.size,
            key.weight,
            key.style,
            FONT_STRETCH_MEDIUM
        ).let { if(it != 0) fontCreateError(family) }
        val fontPtr = font.value ?: fontCreateError(family)
        fonts[key] = fontPtr
        fontPtr
    }
    fun brush(key:Color) = brushes[key] ?: memScoped {
        val brush = nativeHeap.alloc<CPointerVar<d2dSolidColorBrushHolder>>()
        d2dCreateSolidColorBrush(
            target ?: nullPtrError(),
            brush.ptr,
            key.r.toFloat() / 255f,
            key.g.toFloat() / 255f,
            key.b.toFloat() / 255f,
            1f
        )
        val brushPtr = brush.value ?: brushCreateError()
        brushes[key] = brushPtr
        brushPtr
    }
}