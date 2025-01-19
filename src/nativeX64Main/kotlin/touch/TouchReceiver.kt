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
        constructor(info:TouchInfo):this(info.pointX.toFloat(),info.pointY.toFloat(),info.id)
    }
    fun down(info: TouchEvent):Boolean
    fun up(info: TouchEvent):Boolean
    fun move(info: TouchEvent):Boolean
}

@OptIn(ExperimentalForeignApi::class)
fun TouchInfo.toEvent() = TouchReceiver.TouchEvent(this)