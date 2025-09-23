package group

import node.Group
import geometry.Point
import sendInput.moveCursor
import sendInput.scroll
import touch.TouchReceiver

open class MovePointGroup(
    group: Group,
    val sensitivity: Float,
    private val onMovePoint:(Float, Point, TouchReceiver.TouchEvent) -> Unit
) : NormalGroup(group) {
    private var lastTouchPoint: Point? = null
    override fun move(event: TouchReceiver.TouchEvent): Boolean {
        if(pointers[event.id] == null) return true
        onMovePoint(sensitivity,lastTouchPoint ?: return false, event)
        lastTouchPoint = Point(event.x, event.y)
        return true
    }
    override fun down(event: TouchReceiver.TouchEvent): Boolean {
        return super.down(event).apply {
            if(this) lastTouchPoint = Point(event.x, event.y)
        }
    }
}

class MouseGroup(
    group: Group,
    sensitivity: Float
): MovePointGroup(group,sensitivity,::moveCursor)

class ScrollGroup(
    group: Group,
    sensitivity: Float
): MovePointGroup(group,sensitivity,::scroll)