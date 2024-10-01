package buttonGroup

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
    val point = alloc<POINT>().apply {
        GetCursorPos(this.ptr)
        println("$x $y     $xMove $yMove")
    }
    val screenWidth = GetSystemMetrics(SM_CXSCREEN)
    val screenHeight = GetSystemMetrics(SM_CYSCREEN)
    val input:INPUT = alloc<INPUT>().apply {
        type = INPUT_MOUSE.toUInt()
        mi.dwFlags = (MOUSEEVENTF_MOVE_NOCOALESCE or MOUSEEVENTF_MOVE or MOUSEEVENTF_ABSOLUTE).toUInt()
        mi.dx =(xMove + point.x) * 65536 / screenWidth
        mi.dy =(yMove + point.y) * 65536 / screenHeight
    }
    SendInput(1u,input.ptr, sizeOf<INPUT>().toInt())
}