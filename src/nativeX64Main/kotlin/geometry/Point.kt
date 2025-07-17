package geometry

import kotlinx.serialization.Serializable

@Serializable
class Point (
    val x:Float,
    val y:Float
){
    operator fun plus(other: Point) = Point(x + other.x,y + other.y)
    operator fun times(scale: Float) = Point(x*scale,y*scale)
}