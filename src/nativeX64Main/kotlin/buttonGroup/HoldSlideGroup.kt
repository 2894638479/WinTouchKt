package buttonGroup

import button.Button
import button.Point
import error.holdIndexError
import error.nullPtrError
import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.TouchInfo

@OptIn(ExperimentalForeignApi::class)
class HoldSlideGroup(
    buttons: List<Button>,
    offset: Point,
    private val holdIndex: Int
) : NormalGroup(buttons,offset){
    init {
        if(holdIndex !in buttons.indices) holdIndexError(holdIndex)
    }
    private inline val holdButton get() = buttons[holdIndex]
    override fun dispatchMoveEvent(info: TouchInfo, invalidate: (Button) -> Unit) {
        val btns = pointers[info.id] ?: nullPtrError()
        firstOrNull(info.pointX,info.pointY)?.apply {
            if(btns[0] == holdButton){
                if(this != holdButton) {
                    if (btns.size == 1) {
                        down(invalidate)
                        btns.add(this)
                    } else {
                        slide(btns[1], this, btns, invalidate)
                    }
                } else {
                    if(btns.size == 2){
                        btns[1].up(invalidate)
                        btns.removeAt(1)
                    }
                }
            } else {
                if(this != holdButton) {
                    slide(btns[0], this, btns, invalidate)
                }
            }
        }
    }
}