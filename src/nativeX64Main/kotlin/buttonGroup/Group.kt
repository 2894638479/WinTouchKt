package buttonGroup

import button.Button
import button.Point
import error.emptyGroupError
import error.nullPtrError
import kotlinx.cinterop.ExperimentalForeignApi
import touch.TouchReceiver

@ExperimentalForeignApi
abstract class Group(
    val buttons: List<Button>,
){
    init {
        if(buttons.isEmpty()) emptyGroupError()
    }
    val rect = run {
        val mutableRect = buttons[0].shape.outerRect.toMutableRect()
        buttons.forEach { mutableRect += it.shape.outerRect }
        mutableRect.toRect()
    }
    protected val pointers = mutableMapOf<UInt,MutableList<Button>>()
    abstract fun dispatchMoveEvent(event: TouchReceiver.TouchEvent, invalidate:(Button)->Unit)
    abstract fun dispatchDownEvent(event: TouchReceiver.TouchEvent, invalidate:(Button)->Unit):Boolean
    open fun dispatchUpEvent(event: TouchReceiver.TouchEvent, invalidate:(Button)->Unit){
        pointers[event.id]?.forEach { it.up(invalidate) } ?: nullPtrError()
        pointers.remove(event.id)
    }

    protected inline fun firstOrNull(x: Float, y: Float):Button? {
        if (!rect.containPoint(x,y)) return null
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