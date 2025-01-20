package json

import kotlinx.serialization.Serializable

@Serializable
data class ButtonStyleJson(
    val color: ColorJson? = null,
    val textColor: ColorJson? = null,
    val outlineColor: ColorJson? = null,
    val fontFamily:String? = null,
    val fontSize:Float? = null,
    val fontStyle:String? = null,
    val fontWeight:Int? = null,
){
    fun setDefault(default: ButtonStyleJson) = ButtonStyleJson(
        color ?: default.color,
        textColor ?: default.textColor,
        outlineColor ?: default.outlineColor,
        fontFamily ?: default.fontFamily,
        fontSize ?: default.fontSize,
        fontStyle ?: default.fontStyle,
        fontWeight ?: default.fontWeight
    )
}