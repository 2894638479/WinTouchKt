package touch

import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.TouchInfo

@OptIn(ExperimentalForeignApi::class)
interface TouchReceiver {
    fun down(info: TouchInfo):Boolean
    fun up(info: TouchInfo):Boolean
    fun move(info: TouchInfo):Boolean
}