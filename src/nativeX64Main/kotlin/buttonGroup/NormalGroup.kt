package buttonGroup

import button.Button
import button.Point
import button.inRect
import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.TouchInfo

@ExperimentalForeignApi
open class NormalGroup(
    buttons: List<Button>,
    offset: Point,
) : Group(buttons,offset) {
    override fun dispatchMoveEvent(info: TouchInfo, invalidate: (Button) -> Unit) {}
    override fun dispatchDownEvent(info: TouchInfo, invalidate: (Button) -> Unit): Boolean {
        firstOrNull(info.pointX,info.pointY)?.run{
            pointers[info.id] = mutableListOf(this)
            down(invalidate)
            return true
        } ?: return false
    }
}