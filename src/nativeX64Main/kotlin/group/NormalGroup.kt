package group

import node.Group
import touch.GroupTouchDispatcher
import touch.GroupTouchReceiver
import touch.TouchReceiver

class NormalGroup : GroupTouchDispatcher() {
    open class Receiver(group:Group): GroupTouchReceiver(group){
        override fun move(event: TouchReceiver.TouchEvent) = true
        override fun down(event: TouchReceiver.TouchEvent): Boolean {
            return event.touched?.let{
                pointers[event.id] = mutableListOf(it)
                it.down()
            } != null
        }
    }
    override fun create(group: Group) = Receiver(group)
}