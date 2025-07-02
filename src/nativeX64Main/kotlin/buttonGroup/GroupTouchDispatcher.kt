package buttonGroup

import button.Button
import button.Rect
import error.nullPtrError
import touch.TouchReceiver
import wrapper.WeakRefNonNull

abstract class GroupTouchDispatcher(group: Group) {
    private val group by WeakRefNonNull(group)
    val buttons:List<Button> get() = group.buttons
    val rect: Rect? get() = group.cache.outerRect
    protected val pointers = mutableMapOf<UInt,MutableList<Button>>()
    abstract fun dispatchMoveEvent(event: TouchReceiver.TouchEvent, invalidate:(Button)->Unit)
    abstract fun dispatchDownEvent(event: TouchReceiver.TouchEvent, invalidate:(Button)->Unit):Boolean
    open fun dispatchUpEvent(event: TouchReceiver.TouchEvent, invalidate:(Button)->Unit){
        pointers[event.id]?.forEach { it.up(invalidate) } ?: nullPtrError()
        pointers.remove(event.id)
    }
    open fun clearState() = pointers.clear()

    protected inline fun firstOrNull(x: Float, y: Float):Button? {
        if (rect?.containPoint(x,y) != true) return null
        return buttons.firstOrNull { it.inArea(x,y) }
    }
    protected inline fun alreadyDown(button: Button,id: UInt):Boolean {
        return pointers[id]?.contains(button) ?: false
    }

    protected inline fun slide(toUp:Button, toDown:Button, pressedButtons:MutableList<Button>, noinline invalidate: (Button) -> Unit){
        toUp.slideUp(invalidate) { !toDown.key.contains(it) }
        toDown.slideDown(invalidate) { !toUp.key.contains(it) }
        pressedButtons.remove(toUp)
        pressedButtons.add(toDown)
    }
}