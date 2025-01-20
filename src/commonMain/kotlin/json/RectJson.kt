package json

import kotlinx.serialization.Serializable

@Serializable
class RectJson(
    val left:Float,
    val top:Float,
    val right:Float,
    val bottom:Float
)