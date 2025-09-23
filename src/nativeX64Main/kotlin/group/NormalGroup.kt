package group

import node.Group
import touch.TouchReceiver

open class NormalGroup(
    group: Group
) : GroupTouchDispatcher(group) {
    override fun move(event: TouchReceiver.TouchEvent) = true
    override fun down(event: TouchReceiver.TouchEvent): Boolean {
        return event.touched?.let{
            pointers[event.id] = mutableListOf(it)
            it.down()
        } != null
    }
}