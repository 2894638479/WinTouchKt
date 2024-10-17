package button

import container.Container
import draw.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import sendInput.*

@Serializable
class Button(
    val name:String,
    val key:List<UByte>,
    val rect:Rect,
    override var textColor:Color? = null,
    override var textColorPressed: Color? = null,
    override var color:Color? = null,
    override var colorPressed:Color? = null,
    override var textSize:Byte? = null,
):HasButtonConfigs {
    @Transient var pointerId = 0u
        private set
    inline val pressed get() = pointerId != 0u
    fun press(pointer:UInt,invalidate:(Button)->Unit){
        sendAllKeyEvent(KEYEVENT_DOWN)
        pointerId = pointer
        invalidate(this)
    }
    fun up(invalidate:(Button)->Unit){
        sendAllKeyEvent(KEYEVENT_UP)
        pointerId = 0u
        invalidate(this)
    }
    fun slidePress(pointer:UInt,invalidate:(Button)->Unit,filter:(UByte)->Boolean){
        sendAllKeyEventFilter(KEYEVENT_DOWN,filter)
        pointerId = pointer
        invalidate(this)
    }
    fun slideUp(invalidate:(Button)->Unit,filter:(UByte)->Boolean){
        sendAllKeyEventFilter(KEYEVENT_UP,filter)
        pointerId = 0u
        invalidate(this)
    }
}