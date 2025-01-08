package buttonGroup

import button.Button
import button.Point
import error.nullPtrError
import error.slideCountError
import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.TouchInfo

@OptIn(ExperimentalForeignApi::class)
class SlideGroup(
    buttons: List<Button>,
    offset: Point,
    private val slideCount: UInt
) : NormalGroup(buttons,offset) {
    init {
        if (slideCount < 1u) slideCountError(slideCount)
    }
    override fun dispatchMoveEvent(info: TouchInfo, invalidate: (Button) -> Unit) {
        firstOrNull(info.pointX,info.pointY)?.run{
            if(alreadyDown(this,info.id)) return
            val pressedButtons = pointers[info.id] ?: nullPtrError()
            if (pressedButtons.size.toUInt() < slideCount){
                pointers[info.id]?.add(this) ?: nullPtrError()
                this.down(invalidate)
            } else {
                val toUp = pressedButtons[0]
                val toDown = this
                slide(toUp,toDown,pressedButtons, invalidate)
            }
        }
    }
}