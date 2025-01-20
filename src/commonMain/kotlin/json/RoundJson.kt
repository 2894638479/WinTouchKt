package json

import kotlinx.serialization.Serializable

@Serializable
class RoundJson(
    val x:Float,
    val y:Float,
    val r:Float,
    val outlineWidth:Float = 0f
)