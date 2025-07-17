package group

import node.Group
import touch.TouchReceiver

class HoldSlideGroup(
    group: Group,
    val holdIndex: Int
) : NormalGroup(group){
    init {
        if(holdIndex !in buttons.indices) error("hold index not in button indices")
    }
    private inline val holdButton get() = buttons[holdIndex]
    override fun move(event: TouchReceiver.TouchEvent): Boolean {
        val btns = pointers[event.id] ?: error("pointer id not down")
        firstOrNull(event.x, event.y)?.apply {
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