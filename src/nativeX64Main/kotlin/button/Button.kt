package button

import sendInput.KEYEVENT_DOWN
import sendInput.KEYEVENT_UP
import sendInput.sendAllKeyEvent
import sendInput.sendAllKeyEventFilter

class Button(
    val name:String,
    val key:List<UByte>,
    val shape:Shape,
    val style: ButtonStyle,
    val stylePressed: ButtonStyle,
){
    inline val currentStyle get() = if(pressed) stylePressed else style
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
    fun downNoKey(invalidate:(Button)->Unit){
        count++
        invalidate(this)
    }
    fun upNoKey(invalidate: (Button) -> Unit){
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
    fun inArea(x:Float,y:Float) = shape.containPoint(x,y)
}