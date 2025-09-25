package group

import dsl.mutStateOf
import node.Group
import touch.TouchReceiver
import kotlin.math.max

class SlideGroup(
    group: Group,
    slideCount: UInt
) : NormalGroup(group) {
    var slideCount by mutStateOf(slideCount){ max(it,1u) }
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