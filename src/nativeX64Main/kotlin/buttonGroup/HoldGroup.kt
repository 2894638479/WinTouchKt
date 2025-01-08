package buttonGroup

import button.Button
import button.Point
import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.TouchInfo

@OptIn(ExperimentalForeignApi::class)
class HoldGroup(
    buttons: List<Button>,
    offset: Point,
) : Group(buttons,offset) {
    override fun dispatchMoveEvent(info: TouchInfo, invalidate: (Button) -> Unit) {}
    override fun dispatchDownEvent(info: TouchInfo, invalidate: (Button) -> Unit): Boolean {
        firstOrNull(info.pointX,info.pointY)?.run{
            if(pressed) up(invalidate)
            else down(invalidate)
            return true
        } ?: return false
    }
    override fun dispatchUpEvent(info: TouchInfo, invalidate: (Button) -> Unit) {}
}