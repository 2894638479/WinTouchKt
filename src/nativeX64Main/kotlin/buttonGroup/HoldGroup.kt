package buttonGroup

import button.Button
import button.Point
import kotlinx.cinterop.ExperimentalForeignApi
import touch.TouchReceiver

@OptIn(ExperimentalForeignApi::class)
class HoldGroup(
    buttons: List<Button>,
) : Group(buttons) {
    override fun dispatchMoveEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit) {}
    override fun dispatchDownEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit): Boolean {
        firstOrNull(event.x, event.y)?.run{
            if(pressed) up(invalidate)
            else down(invalidate)
            return true
        } ?: return false
    }
    override fun dispatchUpEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit) {}
}