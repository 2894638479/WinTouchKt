package sendInput

import geometry.Point
import kotlinx.cinterop.*
import platform.windows.*
import touch.TouchReceiver
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class)
fun moveCursor(delta:Point) = memScoped {
    val xMove = delta.x.roundToInt()
    val yMove = delta.y.roundToInt()
    if(xMove == 0 && yMove == 0) return@memScoped
    sendInput {
        type = INPUT_MOUSE.toUInt()
        mi.dwFlags = (MOUSEEVENTF_MOVE).toUInt()
        mi.dx = xMove
        mi.dy = yMove
    }
}