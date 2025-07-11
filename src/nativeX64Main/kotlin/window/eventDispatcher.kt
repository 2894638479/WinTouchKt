package window

import error.catchInKotlin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.windows.KillTimer
import platform.windows.SetTimer
import platform.windows.TIMERPROC

private val events = mutableMapOf<Int,()->Unit>()
private var curId = 1
    get() = field.apply { field++ }

@OptIn(ExperimentalForeignApi::class)
fun dispatch(delay:UInt = 0u, event:()->Unit){
    val id = curId
    events[id] = event
    SetTimer(null,id.toULong(),delay, timeProc)
}

@OptIn(ExperimentalForeignApi::class)
private val timeProc:TIMERPROC = staticCFunction { hwnd,_,id,dwTime ->
    catchInKotlin {
        events[id.toInt()]?.invoke()
        events.remove(id.toInt())
        KillTimer(hwnd,id)
    }
}