package json

import kotlinx.serialization.Serializable

@Serializable
class RoundedRectJson(
    val x:Float,
    val y:Float,
    val w:Float,
    val h:Float,
    val r:Float
)