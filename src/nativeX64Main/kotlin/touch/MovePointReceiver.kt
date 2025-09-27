package touch

import node.Group
import geometry.Point
import group.NormalGroup

abstract class MovePointReceiver(group: Group) : NormalGroup.Receiver(group) {
    abstract val sensitivity: Float
    abstract fun onMovePoint(delta:Point)
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