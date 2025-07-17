package group

import node.Group
import platform.windows.GetTickCount64
import touch.TouchReceiver

class HoldGroupDoubleClk(
    group: Group,
    val ms: ULong
) : GroupTouchDispatcher(group) {
    private var lastUpTime = buttons.map { 0uL }.toULongArray()
    override fun notifyButtonsChanged() {
        lastUpTime = buttons.map { 0uL }.toULongArray()
    }
    override fun move(event: TouchReceiver.TouchEvent) = true
    override fun down(event: TouchReceiver.TouchEvent): Boolean {
        firstOrNull(event.x, event.y)?.run{
            pointers[event.id] = mutableListOf(this)
            if(pressed) up()
            else down()
            return true
        } ?: return false
    }
    override fun up(event: TouchReceiver.TouchEvent): Boolean {
        val time = GetTickCount64()
        val btn = pointers[event.id]?.get(0) ?: error("pointer id not down")
        pointers.remove(event.id)
        val index = buttons.indexOf(btn)
        if(time - lastUpTime[index] > ms){
            if(btn.pressed) btn.up()
            else btn.down()
        }
        lastUpTime[index] = time
        return true
    }
}