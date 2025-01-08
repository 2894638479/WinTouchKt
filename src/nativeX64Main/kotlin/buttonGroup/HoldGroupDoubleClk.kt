package buttonGroup

import button.Button
import button.Point
import error.nullPtrError
import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.TouchInfo
import platform.windows.GetTickCount64

@OptIn(ExperimentalForeignApi::class)
class HoldGroupDoubleClk(
    buttons: List<Button>,
    offset: Point,
    private val ms: ULong
) : Group(buttons,offset) {
    private val lastClkTime = buttons.map { 0uL }.toULongArray()
    override fun dispatchMoveEvent(info: TouchInfo, invalidate: (Button) -> Unit) {}
    override fun dispatchDownEvent(info: TouchInfo, invalidate: (Button) -> Unit): Boolean {
        firstOrNull(info.pointX,info.pointY)?.run{
            pointers[info.id] = mutableListOf(this)
            if(pressed) up(invalidate)
            else down(invalidate)
            return true
        } ?: return false
    }
    override fun dispatchUpEvent(info: TouchInfo, invalidate: (Button) -> Unit) {
        val time = GetTickCount64()
        val btn = pointers[info.id]?.get(0) ?: nullPtrError()
        pointers.remove(info.id)
        val index = buttons.indexOf(btn)
        if(time - lastClkTime[index] > ms){
            if(btn.pressed) btn.up(invalidate)
            else btn.down(invalidate)
        }
        lastClkTime[index] = time
    }
}