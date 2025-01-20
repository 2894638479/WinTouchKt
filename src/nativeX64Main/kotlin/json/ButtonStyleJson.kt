package json

import button.ButtonStyle
import draw.Color
import kotlinx.serialization.Serializable

@Serializable
data class ButtonStyleJson(
    val color: Color? = null,
    val textColor: Color? = null,
    val outlineColor: Color? = null,
    val fontFamily:String? = null,
    val fontSize:Float? = null,
    val fontStyle:String? = null,
    val fontWeight:Int? = null,
){
    fun toButtonStyle(pressed:Boolean,scale:Float) = ButtonStyle(
        pressed,
        scale,
        color,
        textColor,
        outlineColor,
        fontFamily,
        fontSize,
        fontStyle,
        fontWeight
    )
    fun setDefault(default:ButtonStyleJson) = ButtonStyleJson(
        color ?: default.color,
        textColor ?: default.textColor,
        outlineColor ?: default.outlineColor,
        fontFamily ?: default.fontFamily,
        fontSize ?: default.fontSize,
        fontStyle ?: default.fontStyle,
        fontWeight ?: default.fontWeight
    )
}