package group

import node.Group
import touch.GroupTouchDispatcher
import touch.GroupTouchReceiver
import touch.TouchReceiver

class HoldGroup: GroupTouchDispatcher() {
    override fun create(group: Group) = object : GroupTouchReceiver(group){
        override fun move(event: TouchReceiver.TouchEvent) = true
        override fun down(event: TouchReceiver.TouchEvent): Boolean {
            return event.touched?.run{
                if(pressed) up()
                else down()
            } != null
        }
        override fun up(event: TouchReceiver.TouchEvent) = true
    }
}