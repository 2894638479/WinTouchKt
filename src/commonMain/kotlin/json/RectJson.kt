package json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RectJson(
    val x:Float,
    val y:Float,
    val w:Float,
    val h:Float
)