package sendInput

import button.Point
import kotlinx.cinterop.*
import libs.Clib.TouchInfo
import platform.windows.*

@OptIn(ExperimentalForeignApi::class)
fun moveCursor(sensitivity:Float,before:Point,moved:TouchInfo) = memScoped {
    fun getMoveDistance(before:Int,after:Int):Int{
        return ((after - before) * sensitivity).toInt()
    }
    val xMove = getMoveDistance(before.x,moved.pointX)
    val yMove = getMoveDistance(before.y,moved.pointY)
    if(xMove == 0 && yMove == 0) return@memScoped
    sendInput {
        type = INPUT_MOUSE.toUInt()
        mi.dwFlags = (MOUSEEVENTF_MOVE_NOCOALESCE or MOUSEEVENTF_MOVE).toUInt()
        mi.dx = xMove
        mi.dy = yMove
    }
}