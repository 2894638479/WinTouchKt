package buttonGroup

import button.Point
import platform.windows.GetTickCount64
import sendInput.moveCursor
import touch.TouchReceiver
import window.dispatch

class TouchPadGroup(
    group: Group,
    val sensitivity:Float,
    val ms:ULong
):GroupTouchDispatcher(group) {
    private var lastTouchPoint : Point? = null
    private val lastDownTime = buttons.map { 0uL }.toULongArray()
    private val keyDownCount = buttons.map { 0u }.toUIntArray()

    override fun move(event: TouchReceiver.TouchEvent): Boolean {
        if(pointers[event.id] == null) return true
        moveCursor(sensitivity,lastTouchPoint ?: error("lastTouchPoint is null") , event)
        lastTouchPoint = Point(event.x, event.y)
        return true
    }

    override fun down(event: TouchReceiver.TouchEvent): Boolean {
        return firstOrNull(event.x,event.y)?.let{
            pointers[event.id] = mutableListOf(it)
            lastTouchPoint = Point(event.x,event.y)
            val index = buttons.indexOf(it)
            val time = GetTickCount64()
            if(time - lastDownTime[index] < ms){
                it.down()
                keyDownCount[index]++
            } else {
                it.down { false }
            }
            lastDownTime[index] = time
        } != null
    }

    override fun up(event: TouchReceiver.TouchEvent): Boolean {
        pointers[event.id]?.get(0)?.let {
            val index = buttons.indexOf(it)
            val time = GetTickCount64()
            if(keyDownCount[index] > 0u){
                it.up()
                keyDownCount[index]--
                return@let
            }
            it.up { false }
            if(time - lastDownTime[index] < ms){
                dispatch(ms.toUInt()) {
                    if(keyDownCount[index] == 0u) {
                        it.down(false)
                        it.up(false)
                    }
                }
            }
        } ?: error("pointer id not down")
        return true
    }
}