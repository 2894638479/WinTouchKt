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
    operator fun unaryMinus() = Point(-x,-y)
    operator fun div(scale:Float) = times(1/scale)
    override fun toString() = "Point${Json.encodeToString(this)}"
}

operator fun Point?.unaryMinus() = this?.unaryMinus()

operator fun Point?.plus(other:Point?):Point? {
    return this?.plus(other ?: return null) ?: other
}
operator fun Point?.minus(other:Point?):Point? {
    return this?.minus(other ?: return null) ?: -other
}