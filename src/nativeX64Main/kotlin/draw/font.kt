package draw

import button.HasButtonConfigs
import kotlinx.cinterop.*
import platform.windows.*

@OptIn(ExperimentalForeignApi::class)
val defaultFont = GetStockObject(DEFAULT_GUI_FONT) as HFONT?

@OptIn(ExperimentalForeignApi::class)
private val fontList = mutableMapOf<Byte,HFONT?>()
@OptIn(ExperimentalForeignApi::class)
val HasButtonConfigs.font:HFONT? get() {
    val key = textSize ?: return defaultFont
    return fontList[key] ?: memScoped {
        val lf = alloc<LOGFONT>()
        GetObject!!(defaultFont, sizeOf<LOGFONT>().toInt(),lf.ptr)
        textSize?.apply { lf.lfHeight = toInt() }
        CreateFontIndirect!!(lf.ptr)
    }.apply { fontList[key] = this }
}

@OptIn(ExperimentalForeignApi::class)
fun deleteAllFont(){
    fontList.forEach {
        DeleteObject(it.value)
    }
    fontList.clear()
}