package json

import geometry.RoundedRect
import kotlinx.serialization.Serializable

@Serializable
class RoundedRectJson(
    val x:Float,
    val y:Float,
    val w:Float,
    val h:Float,
    val r:Float
){
    fun toRoundedRect() = RoundedRect(
        x - 0.5f * w,
        y - 0.5f * h,
        x + 0.5f * w,
        y + 0.5f * h,
        r
    )
}