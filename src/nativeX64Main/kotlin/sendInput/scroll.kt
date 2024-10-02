package sendInput

import button.Point
import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.TouchInfo
import platform.windows.MOUSEEVENTF_HWHEEL
import platform.windows.MOUSEEVENTF_WHEEL
import platform.windows.mouse_event
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class)
fun scroll(sensitivity:Float, before: Point, moved: TouchInfo){
    fun getMoveDistance(before:Int,after:Int):Int{
        return ((after - before) * sensitivity).roundToInt()
    }
    val xMove = getMoveDistance(before.x,moved.pointX)
    val yMove = getMoveDistance(before.y,moved.pointY)
    if(yMove != 0){
        mouse_event(MOUSEEVENTF_WHEEL.toUInt(), 0u, 0u, (- yMove).toUInt(), 0u)
    }
    if(xMove != 0){
        mouse_event(MOUSEEVENTF_HWHEEL.toUInt(), 0u, 0u, xMove.toUInt(), 0u)
    }
}