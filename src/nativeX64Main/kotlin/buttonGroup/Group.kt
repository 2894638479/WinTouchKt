package buttonGroup

import button.Button
import button.HasButtonConfigs
import button.Point
import button.inRect
import draw.Color
import error.emptyGroupError
import error.nullPtrError
import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.TouchInfo
import platform.posix.remove

@ExperimentalForeignApi
abstract class Group(
    val buttons: List<Button>,
    private val offset: Point,
    override var textColor: Color? = null,
    override var textColorPressed: Color? = null,
    override var color: Color? = null,
    override var colorPressed: Color? = null,
    override var textSize:Byte? = null,
): HasButtonConfigs {
    init {
        if(buttons.isEmpty()) emptyGroupError()
    }
    val rect = run {
        buttons.forEach { it.rect += offset }
        buttons[0].rect.copy().apply {
            buttons.forEach { this += it.rect }
        }
    }
    protected val pointers = mutableMapOf<UInt,MutableList<Button>>()
    abstract fun dispatchMoveEvent(info: TouchInfo, invalidate:(Button)->Unit)
    abstract fun dispatchDownEvent(info: TouchInfo, invalidate:(Button)->Unit):Boolean
    open fun dispatchUpEvent(info: TouchInfo, invalidate:(Button)->Unit){
        pointers[info.id]?.forEach { it.up(invalidate) } ?: nullPtrError()
        pointers.remove(info.id)
    }

    protected inline fun firstOrNull(x:Int,y:Int):Button? {
        if (!inRect(rect,x,y)) return null
        return buttons.firstOrNull { inRect(it.rect,x,y) }
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