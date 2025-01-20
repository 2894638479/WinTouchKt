package json

import kotlinx.serialization.Serializable

@Serializable
data class ButtonJson(
    val name:String,
    val rect:RectJson? = null,
    val round:RoundJson? = null,
    val roundedRect:RoundedRectJson? = null,
    val key:List<UByte>,
    val style: ButtonStyleJson? = null,
    val stylePressed: ButtonStyleJson? = null,
    val outlineWidth:Float = 0f
)