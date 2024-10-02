package buttonGroup

import button.Button
import button.Point
import button.inRect
import kotlinx.cinterop.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import libs.Clib.TouchInfo
import platform.windows.INPUT_MOUSE
import platform.windows.MOUSEEVENTF_WHEEL
import sendInput.moveCursor
import sendInput.scroll
import sendInput.sendInput

@Serializable
@OptIn(ExperimentalForeignApi::class)
class ButtonGroup(
    val buttons: List<Button>,
    val typeValue:Int,
    private val offset: Point
) {
    init {
        if(buttons.isEmpty()) error("creating an empty buttonGroup")
    }
    enum class GroupType{
        Static,Slide,MoveMouse,Scroll
    }
    companion object{
        const val MAX_SENSITIVITY = 1024
    }
    val type:GroupType get() {
        return if (typeValue > 0) GroupType.Slide
        else if (typeValue == 0) GroupType.Static
        else if(typeValue >= -MAX_SENSITIVITY) GroupType.MoveMouse
        else if(typeValue >= -MAX_SENSITIVITY * 2) GroupType.Scroll
        else error("unknown group type: $typeValue")
    }
    val sensitivity:Float get() {
        return when(type){
            GroupType.MoveMouse -> - typeValue / MAX_SENSITIVITY.toFloat() * 10
            GroupType.Scroll -> - (MAX_SENSITIVITY + typeValue) / MAX_SENSITIVITY.toFloat() * 10
            else -> error("type $type shouldn't have sensitivity")
        }
    }
    @Transient val rect = run {
        buttons.forEach { it.rect += offset }
        buttons[0].rect.copy().apply {
            buttons.forEach { this += it.rect }
        }
    }
    private fun isKeyPressed(givenKey:UByte):Boolean{
        for(button in buttons){
            if (button.pressed) {
                for (key in button.key) {
                    if (key == givenKey) return true
                }
            }
        }
        return false
    }
    @Transient private val downButtons = mutableListOf<Button>()
    private fun slideToButton(button:Button, pointer:UInt){
        if(button.pointerId != pointer){
            var removedButton:Button? = null
            if (downButtons.size + 1 > typeValue) {
                downButtons[0].slideUp { !button.key.contains(it) }
                removedButton = downButtons[0]
                downButtons.removeAt(0)
            }
            downButtons.add(button)
            button.slidePress(pointer) { removedButton?.key?.contains(it) != true && !isKeyPressed(it) }
        } else {
            if(button !== downButtons.last()){
                downButtons.remove(button)
                downButtons.add(button)
            }
        }
    }
    private var lastTouchPoint:Point? = null
    fun dispatchMoveEvent(info: TouchInfo) = when(type) {
        GroupType.Static -> {  }
        GroupType.Slide -> {
            for(button in buttons){
                if(info.inRect(button.rect)){
                    slideToButton(button,info.id)
                }
            }
        }
        GroupType.MoveMouse -> lastTouchPoint?.let{
            lastTouchPoint?.let { moveCursor(sensitivity,it,info) }
            lastTouchPoint = Point(info.pointX,info.pointY)
        }
        GroupType.Scroll -> {
            lastTouchPoint?.let { scroll(sensitivity,it,info) }
            lastTouchPoint = Point(info.pointX,info.pointY)
        }
    }
    fun dispatchDownEvent(info: TouchInfo):Boolean {
        if(!info.inRect(rect)){
            return false
        }
        for(button in buttons){
            if(info.inRect(button.rect)){
                downButtons.add(button)
                button.press(info.id)
                lastTouchPoint = Point(info.pointX,info.pointY)
                return true
            }
        }
        return false
    }
    fun dispatchUpEvent(info: TouchInfo) {
        downButtons.removeAll {
            (info.id == it.pointerId).apply {
                if(this){
                    it.up()
                    lastTouchPoint = null
                }
            }
        }
    }
}