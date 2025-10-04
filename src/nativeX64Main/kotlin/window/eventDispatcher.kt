package window

import error.catchInKotlin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import logger.info
import platform.windows.KillTimer
import platform.windows.SetTimer
import platform.windows.TIMERPROC

private val events = mutableMapOf<ULong,()->Unit>()

@OptIn(ExperimentalForeignApi::class)
fun dispatch(delay:UInt = 0u, event:()->Unit){
    val realId = SetTimer(null,1u,delay, timeProc)
    events[realId] = event
}

@OptIn(ExperimentalForeignApi::class)
private val timeProc:TIMERPROC = staticCFunction { hwnd,_,id,dwTime ->
    catchInKotlin {
        events[id]?.invoke()
        events.remove(id)
        KillTimer(hwnd,id)
    }
}