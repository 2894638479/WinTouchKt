package sendInput

import geometry.Point
import platform.windows.MOUSEEVENTF_HWHEEL
import platform.windows.MOUSEEVENTF_WHEEL
import platform.windows.mouse_event
import touch.TouchReceiver
import kotlin.math.roundToInt

fun scroll(delta:Point){
    val xMove = delta.x.roundToInt()
    val yMove = delta.y.roundToInt()
    if(yMove != 0){
        mouse_event(MOUSEEVENTF_WHEEL.toUInt(), 0u, 0u, (- yMove).toUInt(), 0u)
    }
    if(xMove != 0){
        mouse_event(MOUSEEVENTF_HWHEEL.toUInt(), 0u, 0u, xMove.toUInt(), 0u)
    }
}