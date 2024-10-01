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
            field = value
            drawScope.invalidate(this)
            if(key.contains(0u) && !pressed){
                if(drawScope.showStatus){
                    drawScope.hideButtons(this)
                } else {
                    drawScope.showButtons { mainContainer.forEachButton(it) }
                }
            }
        }
    inline val pressed get() = pointerId != 0u
    fun press(pointer:UInt){
        pointerId = pointer
        sendAllKeyEvent(key, KEYEVENT_DOWN)
    }
    fun up(){
        pointerId = 0u
        sendAllKeyEvent(key, KEYEVENT_UP)
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