package json

import geometry.Rect
import kotlinx.serialization.Serializable

@Serializable
class RectJson(
    val x:Float,
    val y:Float,
    val w:Float,
    val h:Float
){
    fun toRect() = Rect(
        x - 0.5f * w,
        y - 0.5f * h,
        x + 0.5f * w,
        y + 0.5f * h
    )
}