package json

import kotlinx.serialization.Serializable

@Serializable
class RoundedRectJson(
    val left:Float,
    val top:Float,
    val right:Float,
    val bottom:Float,
    val r:Float,
    val outlineWidth:Float = 0f
)