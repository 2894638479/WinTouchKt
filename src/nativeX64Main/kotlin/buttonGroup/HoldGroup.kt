package buttonGroup

import button.Button
import button.Point
import kotlinx.cinterop.ExperimentalForeignApi
import touch.TouchReceiver
import kotlin.native.ref.WeakReference

class HoldGroup(
    group: Group
) : GroupTouchDispatcher(group) {
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