package buttonGroup

import button.Button
import button.Point
import error.nullPtrError
import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.TouchInfo
import sendInput.moveCursor
import sendInput.scroll

@OptIn(ExperimentalForeignApi::class)
open class MovePointGroup(
    buttons: List<Button>,
    offset: Point,
    protected val sensitivity: Float,
    private val onMovePoint:(Float,Point,TouchInfo) -> Unit
) : NormalGroup(buttons,offset) {
    private var lastTouchPoint:Point? = null
    override fun dispatchMoveEvent(info: TouchInfo, invalidate: (Button) -> Unit) {
        onMovePoint(sensitivity,lastTouchPoint ?: nullPtrError() ,info)
        lastTouchPoint = Point(info.pointX,info.pointY)
    }
    override fun dispatchDownEvent(info: TouchInfo, invalidate: (Button) -> Unit): Boolean {
        return super.dispatchDownEvent(info, invalidate).apply {
            if(this) lastTouchPoint = Point(info.pointX,info.pointY)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
class MouseGroup(
    buttons: List<Button>,
    offset: Point,
    sensitivity: Float
):MovePointGroup(buttons,offset,sensitivity,::moveCursor)

@OptIn(ExperimentalForeignApi::class)
class ScrollGroup(
    buttons: List<Button>,
    offset: Point,
    sensitivity: Float
):MovePointGroup(buttons,offset,sensitivity,::scroll)