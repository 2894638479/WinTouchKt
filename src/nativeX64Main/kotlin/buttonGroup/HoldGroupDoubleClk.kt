package buttonGroup

import button.Button
import button.Point
import error.nullPtrError
import kotlinx.cinterop.ExperimentalForeignApi
import platform.windows.GetTickCount64
import touch.TouchReceiver

@OptIn(ExperimentalForeignApi::class)
class HoldGroupDoubleClk(
    buttons: List<Button>,
    offset: Point,
    private val ms: ULong
) : Group(buttons,offset) {
    private val lastClkTime = buttons.map { 0uL }.toULongArray()
    override fun dispatchMoveEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit) {}
    override fun dispatchDownEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit): Boolean {
        firstOrNull(event.x, event.y)?.run{
            pointers[event.id] = mutableListOf(this)
            if(pressed) up(invalidate)
            else down(invalidate)
            return true
        } ?: return false
    }
    override fun dispatchUpEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit) {
        val time = GetTickCount64()
        val btn = pointers[event.id]?.get(0) ?: nullPtrError()
        pointers.remove(event.id)
        val index = buttons.indexOf(btn)
        if(time - lastClkTime[index] > ms){
            if(btn.pressed) btn.up(invalidate)
            else btn.down(invalidate)
        }
        lastClkTime[index] = time
    }
}