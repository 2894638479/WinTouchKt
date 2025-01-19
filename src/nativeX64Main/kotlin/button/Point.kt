package button

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import libs.Clib.TouchInfo

@Serializable
class Point (
    val x:Float,
    val y:Float
)

fun inRect(rect:Rect,x:Float,y:Float):Boolean = x > rect.left
        && y > rect.top
        && x < rect.right
        && y < rect.bottom