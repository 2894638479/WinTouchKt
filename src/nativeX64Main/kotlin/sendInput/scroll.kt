package sendInput

import geometry.Point
import platform.windows.MOUSEEVENTF_HWHEEL
import platform.windows.MOUSEEVENTF_WHEEL
import platform.windows.mouse_event
import touch.TouchReceiver
import kotlin.math.roundToInt

fun scroll(sensitivity:Float, before: Point, moved: TouchReceiver.TouchEvent){
    fun getMoveDistance(before:Float,after:Float):Int{
        return ((after - before) * sensitivity).roundToInt()
    }
    val xMove = getMoveDistance(before.x,moved.x)
    val yMove = getMoveDistance(before.y,moved.y)
    if(yMove != 0){
        mouse_event(MOUSEEVENTF_WHEEL.toUInt(), 0u, 0u, (- yMove).toUInt(), 0u)
    }
    if(xMove != 0){
        mouse_event(MOUSEEVENTF_HWHEEL.toUInt(), 0u, 0u, xMove.toUInt(), 0u)
    }
}