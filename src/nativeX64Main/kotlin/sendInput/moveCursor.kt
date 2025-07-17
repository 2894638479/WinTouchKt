package sendInput

import geometry.Point
import kotlinx.cinterop.*
import platform.windows.*
import touch.TouchReceiver
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class)
fun moveCursor(sensitivity:Float, before: Point, moved:TouchReceiver.TouchEvent) = memScoped {
    fun getMoveDistance(before:Float,after:Float):Int{
        return ((after - before) * sensitivity).roundToInt()
    }
    val xMove = getMoveDistance(before.x,moved.x)
    val yMove = getMoveDistance(before.y,moved.y)
    if(xMove == 0 && yMove == 0) return@memScoped
    sendInput {
        type = INPUT_MOUSE.toUInt()
        mi.dwFlags = (MOUSEEVENTF_MOVE).toUInt()
        mi.dx = xMove
        mi.dy = yMove
    }
}