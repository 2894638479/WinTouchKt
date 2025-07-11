package touch

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
    }
    fun down(event: TouchEvent):Boolean
    fun up(event: TouchEvent):Boolean
    fun move(event: TouchEvent):Boolean
}

@OptIn(ExperimentalForeignApi::class)
fun TouchInfo.toEvent() = TouchReceiver.TouchEvent(this)