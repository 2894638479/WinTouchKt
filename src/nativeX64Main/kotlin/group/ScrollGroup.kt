package group

import dsl.mutStateOf
import geometry.Point
import node.Group
import sendInput.scroll
import touch.GroupTouchDispatcher
import touch.MovePointReceiver

class ScrollGroup(sensitivity:Float? = null): GroupTouchDispatcher(){
    var sensitivity by mutStateOf(sensitivity ?: 1f)
    override fun create(group: Group) = object : MovePointReceiver(group){
        override val sensitivity get() = this@ScrollGroup.sensitivity
        override fun onMovePoint(delta: Point) = scroll(delta)
    }
}