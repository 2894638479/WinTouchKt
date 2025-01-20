package button

import kotlinx.serialization.Serializable

@Serializable
class Point (
    val x:Float,
    val y:Float
){
    fun rescaled(scale:Float):Point{
        return Point(x * scale,y * scale)
    }
}

fun inRect(rect:Rect,x:Float,y:Float):Boolean = x > rect.left
        && y > rect.top
        && x < rect.right
        && y < rect.bottom