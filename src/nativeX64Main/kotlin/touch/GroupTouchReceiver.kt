package touch

import node.Button
import node.Group
import wrapper.WeakRef

open class GroupTouchReceiver(group: Group) : TouchReceiver {
    val group by WeakRef(group)
    val buttons:List<Button> get() = group?.buttons ?: emptyList()
    protected val pointers = mutableMapOf<UInt,MutableList<Button>>()
    open fun notifyButtonsChanged(){}
    final override var valid = true
    override fun destroy(){
        if(!valid) return
        pointers.values.forEach { it.forEach { it.upAll() } }
        pointers.clear()
        valid = false
    }

    override fun up(event: TouchReceiver.TouchEvent): Boolean {
        pointers[event.id]?.forEach { it.up() } ?: return false
        pointers.remove(event.id)
        return true
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