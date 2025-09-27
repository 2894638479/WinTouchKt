package group

import node.Group
import touch.GroupTouchDispatcher
import touch.TouchReceiver

class HoldSlideGroup : GroupTouchDispatcher(){
    override fun create(group: Group) = object: NormalGroup.Receiver(group) {
        private inline val holdButton get() = buttons.getOrNull(0)
        override fun move(event: TouchReceiver.TouchEvent): Boolean {
            val btns = pointers[event.id] ?: return false
            event.touched?.apply {
                if(btns[0] == holdButton){
                    if(this != holdButton) {
                        if (btns.size == 1) {
                            down()
                            btns.add(this)
                        } else {
                            slide(btns[1], this, btns)
                        }
                    } else {
                        if(btns.size == 2){
                            btns[1].up()
                            btns.removeAt(1)
                        }
                    }
                } else {
                    if(this != holdButton) {
                        slide(btns[0], this, btns)
                    }
                }
            }
            return true
        }
    }
}