package button

import draw.Color
import draw.GREY_BRIGHT
import draw.GREY_DARK
import kotlinx.serialization.Serializable


@Serializable
class ButtonStyle(
    var color: Color? = null,
    var textColor: Color? = null,
    var outlineColor: Color? = null,
    var fontFamily:String? = null,
    var fontSize:Float? = null,
    var fontStyle:String? = null,
    var fontWeight:Int? = null,
    val outlineWidth:Float? = null
){
    companion object {
        val default = ButtonStyle(
            color = GREY_DARK,
            outlineWidth = 1f
        )
        val defaultPressed = ButtonStyle(
            color = GREY_BRIGHT,
            outlineWidth = 1f
        )
    }
}