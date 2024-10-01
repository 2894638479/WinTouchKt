package button

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import libs.Clib.TouchInfo

@Serializable
class Point (
    val x:Int,
    val y:Int
){
    fun inRect(rect:Rect):Boolean = inRect(rect,x,y)
}



@OptIn(ExperimentalForeignApi::class)
fun TouchInfo.inRect(rect:Rect) = inRect(rect,pointX,pointY)



fun inRect(rect:Rect,x:Int,y:Int):Boolean = x > rect.left
        && y > rect.top
        && x < rect.right
        && y < rect.bottom