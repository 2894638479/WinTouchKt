package group

import dsl.mutStateOf
import node.Group
import platform.windows.GetTickCount64
import touch.TouchReceiver

class HoldGroupDoubleClk(
    group: Group,
    ms: ULong
) : GroupTouchDispatcher(group) {
    var ms by mutStateOf(ms)
    private var lastUpTime = buttons.map { 0uL }.toULongArray()
    override fun notifyButtonsChanged() {
        lastUpTime = buttons.map { 0uL }.toULongArray()
        super.notifyButtonsChanged()
    }
    override fun move(event: TouchReceiver.TouchEvent) = true
    override fun down(event: TouchReceiver.TouchEvent): Boolean {
        return event.touched?.run{
            pointers[event.id] = mutableListOf(this)
            if(pressed) up()
            else down()
        } != null
    }
    override fun up(event: TouchReceiver.TouchEvent): Boolean {
        val time = GetTickCount64()
        val btn = pointers[event.id]?.get(0) ?: return false
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