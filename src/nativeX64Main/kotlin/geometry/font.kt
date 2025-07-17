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
    constructor(family: String?,size: Float?,style: String?,weight: Int?,scale:Float?):this(
        family ?: "",
        (size ?: 24f)*(scale ?: 1f),
        style?.let {
            styles[it] ?: error("font style error, should be ${styles.keys}")
        } ?: FONT_STYLE.FONT_STYLE_NORMAL,
        weight?.let {
            if(it < 1 || it > 999) {
                error("font weight error, should in 1 to 999")
            } else it.toUShort()
        } ?: 500u
    )
    companion object{
        val styles = mapOf(
            "normal" to FONT_STYLE.FONT_STYLE_NORMAL,
            "italic" to FONT_STYLE.FONT_STYLE_ITALIC,
            "oblique" to FONT_STYLE.FONT_STYLE_OBLIQUE
        )
    }
}