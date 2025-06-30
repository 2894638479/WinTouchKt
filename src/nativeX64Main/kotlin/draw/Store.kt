package draw

import error.brushCreateError
import error.fontCreateError
import error.nullPtrError
import kotlinx.cinterop.*
import libs.Clib.*

@OptIn(ExperimentalForeignApi::class)
object Store {
    private val brushes = HashMap<Color, CPointer<d2dSolidColorBrushHolder>>(100)
    private val fonts = HashMap<Font, CPointer<d2dTextFormatHolder>>(100)
    var writeFactory: CValuesRef<d2dWriteFactoryHolder>? = null
    var target: CValuesRef<d2dTargetHolder>? = null
    fun CPointer<d2dTextFormatHolder>.free() = d2dFreeTextFormat(this)
    fun CPointer<d2dSolidColorBrushHolder>.free() = d2dFreeSolidColorBrush(this)
    fun font(key:Font) = fonts[key] ?: createFont(key).apply { fonts[key] = this }
    private fun createFont(font:Font) = memScoped {
        val fontHolder = nativeHeap.alloc<CPointerVar<d2dTextFormatHolder>>()
        d2dCreateTextFormat(
            writeFactory ?: nullPtrError(),
            fontHolder.ptr,
            font.family.wcstr,
            font.size,
            font.weight,
            font.style,
            FONT_STRETCH_MEDIUM
        ).let { if(it != 0) fontCreateError(font.family) }
        fontHolder.value ?: fontCreateError(font.family)
    }
    fun brush(key:Color) = brushes[key] ?: createBrush(key).apply { brushes[key] = this }
    private fun createBrush(key:Color) = memScoped {
        val brush = nativeHeap.alloc<CPointerVar<d2dSolidColorBrushHolder>>()
        d2dCreateSolidColorBrush(
            target ?: nullPtrError(),
            brush.ptr,
            key.r.toFloat() / 255f,
            key.g.toFloat() / 255f,
            key.b.toFloat() / 255f,
            1f
        )
        brush.value ?: brushCreateError()
    }
}