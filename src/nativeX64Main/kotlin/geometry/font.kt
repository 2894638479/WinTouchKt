package geometry

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf16
import libs.Clib.FONT_STYLE
import platform.windows.DEFAULT_CHARSET
import platform.windows.EnumFontFamiliesEx
import platform.windows.EnumFontFamiliesExW
import platform.windows.GetDC
import platform.windows.HDC
import platform.windows.LOGFONT
import platform.windows.LOGFONTW
import platform.windows.ReleaseDC
import platform.windows.tagLOGFONTW
import platform.windows.tagTEXTMETRICW
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.OptIn
import kotlin.String
import kotlin.UInt
import kotlin.UShort
import kotlin.error
import kotlin.invoke
import kotlin.let
import kotlin.toUShort

@OptIn(ExperimentalForeignApi::class)
data class Font(
    val family:String,
    val size:Float,
    val style:FONT_STYLE,
    val weight:UShort
){
    constructor(family: String?,size: Float?,style: Style?,weight: Int?,scale:Float?):this(
        family ?: "",
        (size ?: 24f)*(scale ?: 1f),
        style?.value ?: FONT_STYLE.FONT_STYLE_NORMAL,
        weight?.let {
            if(it !in 1..999) {
                error("font weight error, should in 1 to 999")
            } else it.toUShort()
        } ?: 500u
    )
    enum class Style(val string: String,val value:FONT_STYLE) {
        NORMAL("normal",FONT_STYLE.FONT_STYLE_NORMAL),
        ITALIC("italic",FONT_STYLE.FONT_STYLE_ITALIC),
        OBLIQUE("oblique",FONT_STYLE.FONT_STYLE_OBLIQUE);
        companion object {
            fun byString(str:String) = entries.firstOrNull {
                it.string == str
            } ?: error("unknown font style, should be one of " + entries.joinToString(", "))
        }
    }
    companion object {
        private val set = mutableSetOf<String>()
        val func: CPointer<CFunction<(CPointer<tagLOGFONTW>?, CPointer<tagTEXTMETRICW>?, UInt, Long) -> Int>> =
            staticCFunction { lpelfe, lpntme, fontType, lParam ->
                if (lpelfe != null) {
                    set += lpelfe.pointed.lfFaceName.toKStringFromUtf16()
                }
                1
            }
        val systemFontFamilies:Set<String> get() = memScoped {
            set.clear()
            val hdc = GetDC(null) ?: return set
            val logFont = alloc<tagLOGFONTW>()
            logFont.lfCharSet = DEFAULT_CHARSET.toUByte()
            EnumFontFamiliesExW(hdc,logFont.ptr,func,0L,0u)
            ReleaseDC(null,hdc)
            set
        }
    }
}