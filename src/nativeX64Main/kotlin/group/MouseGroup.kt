package group

import dsl.mutStateOf
import node.Group
import geometry.Point
import node.Button
import sendInput.moveCursor
import sendInput.scroll
import touch.TouchReceiver

open class MovePointGroup(
    group: Group,
    sensitivity: Float,
    private val onMovePoint: (Point) -> Unit
) : NormalGroup(group) {
    var sensitivity by mutStateOf(sensitivity)
    private var lastTouchPoint: Point? = null
    override fun move(event: TouchReceiver.TouchEvent): Boolean {
        val buttons = pointers[event.id] ?: return true
        val delta = (event.point - (lastTouchPoint ?: error("no lastTouchPoint"))) * sensitivity
        onMovePoint(delta)
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
): MovePointGroup(group,sensitivity,{moveCursor(it)})

class ScrollGroup(
    group: Group,
    sensitivity: Float
): MovePointGroup(group,sensitivity,{scroll(it)})