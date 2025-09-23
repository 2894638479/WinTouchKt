package group

import node.Button
import node.Group
import geometry.Rect
import logger.warningBox
import touch.TouchReceiver
import wrapper.WeakRefNonNull

abstract class GroupTouchDispatcher(group: Group) : TouchReceiver {
    private val group by WeakRefNonNull(group)
    val buttons:List<Button> get() = group.buttons
    protected val pointers = mutableMapOf<UInt,MutableList<Button>>()
    override fun up(event: TouchReceiver.TouchEvent): Boolean {
        pointers[event.id]?.forEach { it.up() } ?: return false
        pointers.remove(event.id)
        return true
    }
    open fun notifyButtonsChanged(){}

    var destroyed = false
        private set
    override val valid get() = !destroyed
    fun destroy(){
        if(destroyed) error("touchDispatcher already destroyed")
        pointers.values.forEach { it.forEach { it.upAll() } }
        pointers.clear()
        destroyed = true
    }

    private fun firstOrNull(x: Float, y: Float): Button? {
        return buttons.firstOrNull { it.containPoint(x,y) }
    }
    protected val TouchReceiver.TouchEvent.touched: Button? get() = firstOrNull(x,y)
    protected fun alreadyDown(button: Button, id: UInt):Boolean {
        return pointers[id]?.contains(button) ?: false
    }

    protected fun slide(toUp: Button, toDown: Button, pressedButtons:MutableList<Button>){
        toUp.up { !toDown.key.contains(it) }
        toDown.down { !toUp.key.contains(it) }
        pressedButtons.remove(toUp)
        pressedButtons.add(toDown)
    }
}