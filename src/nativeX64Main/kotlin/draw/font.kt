package draw

import button.HasButtonConfigs
import error.fontStyleError
import error.fontWeightError
import kotlinx.cinterop.*
import libs.Clib.FONT_STYLE
import platform.windows.*

@OptIn(ExperimentalForeignApi::class)
data class Font(
    val family:String?,
    val size:Float,
    val style:FONT_STYLE,
    val weight:UShort
){
    constructor(family: String?,size: Float?,style: String?,weight: UShort?):this(
        family,
        size ?: 24f,
        style?.let { styles[it] ?: fontStyleError(it, styles.keys) } ?: FONT_STYLE.FONT_STYLE_NORMAL,
        weight ?: 500u
    )
    companion object{
        val styles = mapOf(
            "normal" to FONT_STYLE.FONT_STYLE_NORMAL,
            "italic" to FONT_STYLE.FONT_STYLE_ITALIC,
            "oblique" to FONT_STYLE.FONT_STYLE_OBLIQUE
        )
    }
    init {
        if(weight < 1u || weight > 999u) fontWeightError(1u,999u,weight)
    }
}