package group

import node.Group
import touch.TouchReceiver

class SlideGroup(
    group: Group,
    val slideCount: UInt
) : NormalGroup(group) {
    init {
        if (slideCount < 1u) error("slide count < 1")
    }
    override fun move(event: TouchReceiver.TouchEvent): Boolean {
        firstOrNull(event.x, event.y)?.run{
            if(alreadyDown(this, event.id)) return true
            val pressedButtons = pointers[event.id] ?: error("pointer id not down")
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