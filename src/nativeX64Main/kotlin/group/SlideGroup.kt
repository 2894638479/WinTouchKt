package group

import dsl.mutStateOf
import node.Group
import touch.GroupTouchDispatcher
import touch.TouchReceiver
import kotlin.math.max

class SlideGroup(slideCount: UInt? = null) : GroupTouchDispatcher() {
    var slideCount by mutStateOf(slideCount?:1u){ max(it,1u) }
    override fun create(group: Group) = object : NormalGroup.Receiver(group){
        override fun move(event: TouchReceiver.TouchEvent): Boolean {
            event.touched?.run{
                if(alreadyDown(this, event.id)) return true
                val pressedButtons = pointers[event.id] ?: return false
                if (pressedButtons.size.toUInt() < slideCount){
                    pressedButtons.add(this)
                    this.down()
                } else {
                    val toUp = pressedButtons[0]
                    val toDown = this
                    slide(toUp,toDown,pressedButtons)
                }
            }
            return true
        }
    }
}