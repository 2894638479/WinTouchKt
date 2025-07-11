package buttonGroup

import button.Button
import button.Rect
import touch.TouchReceiver
import wrapper.WeakRefNonNull

abstract class GroupTouchDispatcher(group: Group) : TouchReceiver {
    private val group by WeakRefNonNull(group)
    val buttons:List<Button> get() = group.buttons
    val rect: Rect? get() = group.cache.outerRect
    protected val pointers = mutableMapOf<UInt,MutableList<Button>>()
    override fun up(event: TouchReceiver.TouchEvent): Boolean {
        pointers[event.id]?.forEach { it.up() } ?: error("pointer id not down")
        pointers.remove(event.id)
        return true
    }
    open fun clearState() = pointers.clear()

    protected inline fun firstOrNull(x: Float, y: Float):Button? {
        if (rect?.containPoint(x,y) != true) return null
        return buttons.firstOrNull { it.inArea(x,y) }
    }
    protected inline fun alreadyDown(button: Button,id: UInt):Boolean {
        return pointers[id]?.contains(button) ?: false
    }

    protected inline fun slide(toUp:Button, toDown:Button, pressedButtons:MutableList<Button>){
        toUp.up { !toDown.key.contains(it) }
        toDown.down { !toUp.key.contains(it) }
        pressedButtons.remove(toUp)
        pressedButtons.add(toDown)
    }
}