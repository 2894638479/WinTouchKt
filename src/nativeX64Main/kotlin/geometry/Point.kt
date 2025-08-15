package geometry

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Point (
    val x:Float,
    val y:Float
){
    companion object {
        val origin = Point(0f,0f)
    }
    operator fun plus(other: Point) = Point(x + other.x,y + other.y)
    operator fun minus(other: Point) = Point(x - other.x,y - other.y)
    operator fun times(scale: Float) = Point(x*scale,y*scale)
    override fun toString() = "Point${Json.encodeToString(this)}"
}