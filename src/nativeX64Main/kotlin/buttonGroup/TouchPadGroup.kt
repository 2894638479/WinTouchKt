package buttonGroup

import button.Button
import button.Point
import error.nullPtrError
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.windows.GetTickCount64
import sendInput.moveCursor
import touch.TouchReceiver

@OptIn(ExperimentalForeignApi::class)
class TouchPadGroup(
    buttons:List<Button>,
    val sensitivity:Float,
    val ms:ULong
):Group(buttons) {
    private var lastTouchPoint : Point? = null
    private val lastDownTime = buttons.map { 0uL }.toULongArray()
    private val keyDownCount = buttons.map { 0u }.toUIntArray()
    private val mutex = Mutex()
    private inline fun <T> withMutex(crossinline block:()->T) = runBlocking {
        mutex.withLock { block() }
    }

    override fun dispatchMoveEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit) = withMutex {
        if(pointers[event.id] == null) return@withMutex
        moveCursor(sensitivity,lastTouchPoint ?: nullPtrError() , event)
        lastTouchPoint = Point(event.x, event.y)
    }

    override fun dispatchDownEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit): Boolean = withMutex {
        firstOrNull(event.x,event.y)?.let{
            pointers[event.id] = mutableListOf(it)
            lastTouchPoint = Point(event.x,event.y)
            val index = buttons.indexOf(it)
            val time = GetTickCount64()
            if(time - lastDownTime[index] < ms){
                it.down(invalidate)
                keyDownCount[index]++
            } else {
                it.downNoKey(invalidate)
            }
            lastDownTime[index] = time
        } != null
    }

    override fun dispatchUpEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit) = withMutex {
        pointers[event.id]?.get(0)?.let {
            val index = buttons.indexOf(it)
            val time = GetTickCount64()
            if(keyDownCount[index] > 0u){
                it.up(invalidate)
                keyDownCount[index]--
                return@let
            }
            it.upNoKey(invalidate)
            if(time - lastDownTime[index] < ms){
                GlobalScope.launch {
                    delay(ms.toLong())
                    withMutex {
                        if(keyDownCount[index] == 0u) {
                            it.down { }
                            it.up { }
                        }
                    }
                }
            }
        } ?: nullPtrError()
    }
}