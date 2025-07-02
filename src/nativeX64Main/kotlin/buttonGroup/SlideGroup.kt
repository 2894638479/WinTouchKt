package buttonGroup

import button.Button
import button.Point
import error.nullPtrError
import error.slideCountError
import kotlinx.cinterop.ExperimentalForeignApi
import touch.TouchReceiver

class SlideGroup(
    group: Group,
    val slideCount: UInt
) : NormalGroup(group) {
    init {
        if (slideCount < 1u) slideCountError(slideCount)
    }
    override fun dispatchMoveEvent(event: TouchReceiver.TouchEvent, invalidate: (Button) -> Unit) {
        firstOrNull(event.x, event.y)?.run{
            if(alreadyDown(this, event.id)) return
            val pressedButtons = pointers[event.id] ?: nullPtrError()
            if (pressedButtons.size.toUInt() < slideCount){
                pointers[event.id]?.add(this) ?: nullPtrError()
                this.down(invalidate)
            } else {
                val toUp = pressedButtons[0]
                val toDown = this
                slide(toUp,toDown,pressedButtons, invalidate)
            }
        }
    }
}