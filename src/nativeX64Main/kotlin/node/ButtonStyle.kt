package node

import geometry.Color
import kotlinx.serialization.Serializable


@Serializable
class ButtonStyle(
    var color: Color? = null,
    var textColor: Color? = null,
    var outlineColor: Color? = null,
    var fontFamily:String? = null,
    var fontSize:Float? = null,
    var fontStyle:String? = null,
    var fontWeight:Int? = null
)