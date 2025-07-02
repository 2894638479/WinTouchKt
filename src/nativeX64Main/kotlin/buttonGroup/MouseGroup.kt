package buttonGroup

import button.Button
import button.Point
import error.nullPtrError
import kotlinx.cinterop.ExperimentalForeignApi
import sendInput.moveCursor
import sendInput.scroll
import touch.TouchReceiver

open class MovePointGroup(
    group:Group,
    val sensitivity: Float,
    private val onMovePoint:(Float,Point,TouchReceiver.TouchEvent) -> Unit
) : NormalGroup(group) {
    private var lastTouchPoint:Point? = null
    override fun dispatchMoveEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit) {
        if(pointers[event.id] == null) return
        onMovePoint(sensitivity,lastTouchPoint ?: nullPtrError() , event)
        lastTouchPoint = Point(event.x, event.y)
    }
    override fun dispatchDownEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit): Boolean {
        return super.dispatchDownEvent(event, invalidate).apply {
            if(this) lastTouchPoint = Point(event.x, event.y)
        }
    }
}

class MouseGroup(
    group:Group,
    sensitivity: Float
):MovePointGroup(group,sensitivity,::moveCursor)

class ScrollGroup(
    group: Group,
    sensitivity: Float
):MovePointGroup(group,sensitivity,::scroll)