package button

import draw.Color
import sendInput.KEYEVENT_DOWN
import sendInput.KEYEVENT_UP
import sendInput.sendAllKeyEvent
import sendInput.sendAllKeyEventFilter

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
    var count = 0u
        private set
    inline val pressed get() = count != 0u
    fun down(invalidate:(Button)->Unit){
        sendAllKeyEvent(KEYEVENT_DOWN)
        count++
        invalidate(this)
    }
    fun up(invalidate:(Button)->Unit){
        sendAllKeyEvent(KEYEVENT_UP)
        count--
        invalidate(this)
    }
    fun slideDown(invalidate:(Button)->Unit,filter:(UByte)->Boolean){
        sendAllKeyEventFilter(KEYEVENT_DOWN,filter)
        count++
        invalidate(this)
    }
    fun slideUp(invalidate:(Button)->Unit,filter:(UByte)->Boolean){
        sendAllKeyEventFilter(KEYEVENT_UP,filter)
        count--
        invalidate(this)
    }
}