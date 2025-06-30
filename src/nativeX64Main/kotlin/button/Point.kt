package button

import kotlinx.serialization.Serializable

@Serializable
class Point (
    val x:Float,
    val y:Float
){
    fun isZero() = x==0f&&y==0f
    fun scale(scale:Float) = Point(x*scale,y*scale)
}