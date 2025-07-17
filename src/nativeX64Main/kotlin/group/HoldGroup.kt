package group

import node.Group
import touch.TouchReceiver

class HoldGroup(
    group: Group
) : GroupTouchDispatcher(group) {
    override fun move(event: TouchReceiver.TouchEvent) = true
    override fun down(event: TouchReceiver.TouchEvent): Boolean {
        firstOrNull(event.x, event.y)?.run{
            if(pressed) up()
            else down()
            return true
        } ?: return false
    }
    override fun up(event: TouchReceiver.TouchEvent) = true
}