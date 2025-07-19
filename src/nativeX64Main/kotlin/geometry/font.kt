package geometry

import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.FONT_STYLE

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
            if(it < 1 || it > 999) {
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
}