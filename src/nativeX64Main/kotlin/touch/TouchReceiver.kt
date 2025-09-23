package touch

import geometry.Point
import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.TouchInfo

@OptIn(ExperimentalForeignApi::class)
interface TouchReceiver {
    data class TouchEvent(
        val x:Float,
        val y:Float,
        val id:UInt
    ){
        constructor(event:TouchInfo):this(event.pointX.toFloat(),event.pointY.toFloat(),event.id)
        val point get() = Point(x, y)
    }
    fun down(event: TouchEvent):Boolean = true
    fun up(event: TouchEvent):Boolean = true
    fun move(event: TouchEvent):Boolean = true
    val valid: Boolean get() = true
}

@OptIn(ExperimentalForeignApi::class)
fun TouchInfo.toEvent() = TouchReceiver.TouchEvent(this)