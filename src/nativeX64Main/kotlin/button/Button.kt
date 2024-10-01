package button

import drawScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mainContainer

@Serializable
class Button(
    val name:String,
    val key:List<UByte>,
    val rect:Rect,
) {
    @Transient var pointerId = 0u
        private set(value) {
            if(field != value) {
                field = value
                drawScope.invalidate(this)
                processSpecialKey(this)
            }
        }
    inline val pressed get() = pointerId != 0u
    fun press(pointer:UInt){
        sendAllKeyEvent(key, KEYEVENT_DOWN)
        pointerId = pointer
    }
    fun up(){
        sendAllKeyEvent(key, KEYEVENT_UP)
        pointerId = 0u
    }
    fun slidePress(pointer:UInt,filter:(UByte)->Boolean){
        sendAllKeyEventFilter(key, KEYEVENT_DOWN,filter)
        pointerId = pointer
    }
    fun slideUp(filter:(UByte)->Boolean){
        sendAllKeyEventFilter(key, KEYEVENT_UP,filter)
        pointerId = 0u
    }
}