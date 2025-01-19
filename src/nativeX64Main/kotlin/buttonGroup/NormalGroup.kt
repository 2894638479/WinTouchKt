package buttonGroup

import button.Button
import button.Point
import kotlinx.cinterop.ExperimentalForeignApi
import touch.TouchReceiver

@ExperimentalForeignApi
open class NormalGroup(
    buttons: List<Button>,
    offset: Point,
) : Group(buttons,offset) {
    override fun dispatchMoveEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit) {}
    override fun dispatchDownEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit): Boolean {
        firstOrNull(event.x, event.y)?.run{
            pointers[event.id] = mutableListOf(this)
            down(invalidate)
            return true
        } ?: return false
    }
}