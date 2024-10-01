package touch

import button.Point
import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.TouchInfo

@OptIn(ExperimentalForeignApi::class)
interface TouchReceiver {
    fun down(info: TouchInfo)
    fun up(info: TouchInfo)
    fun move(info: TouchInfo)
}