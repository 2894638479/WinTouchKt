package buttonGroup

import button.Button
import button.HasButtonConfigs
import button.Point
import button.inRect
import draw.Color
import error.emptyGroupError
import error.groupTypeError
import error.logicError
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import libs.Clib.TouchInfo
import sendInput.moveCursor
import sendInput.scroll

@OptIn(ExperimentalForeignApi::class)
@Serializable
class ButtonGroup(
    val buttons: List<Button>,
    private val typeValue:Int,
    private val offset: Point,
    override var textColor:Color? = null,
    override var textColorPressed: Color? = null,
    override var color:Color? = null,
    override var colorPressed:Color? = null,
    override var textSize:Byte? = null,
):HasButtonConfigs {
    init {
        if(buttons.isEmpty()) emptyGroupError()
    }
    override fun copyConfig(other: HasButtonConfigs) {
        super.copyConfig(other)
        buttons.forEach {
            it.copyConfig(this)
        }
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
        else groupTypeError(typeValue)
    }
    val sensitivity:Float get() {
        return when(type){
            GroupType.MoveMouse -> - typeValue / MAX_SENSITIVITY.toFloat() * 10
            GroupType.Scroll -> - (MAX_SENSITIVITY + typeValue) / MAX_SENSITIVITY.toFloat() * 10
            else -> logicError("type $type shouldn't have sensitivity")
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
    private fun slideToButton(button:Button, pointer:UInt,invalidate:(Button)->Unit){
        if(button.pointerId != pointer){
            var removedButton:Button? = null
            if (downButtons.size + 1 > typeValue) {
                downButtons[0].slideUp(invalidate) { !button.key.contains(it) }
                removedButton = downButtons[0]
                downButtons.removeAt(0)
            }
            downButtons.add(button)
            button.slidePress(pointer,invalidate) { removedButton?.key?.contains(it) != true && !isKeyPressed(it) }
        } else {
            if(button !== downButtons.last()){
                downButtons.remove(button)
                downButtons.add(button)
            }
        }
    }
    private var lastTouchPoint:Point? = null
    fun dispatchMoveEvent(info: TouchInfo,invalidate:(Button)->Unit) = when(type) {
        GroupType.Static -> {  }
        GroupType.Slide -> {
            for(button in buttons){
                if(info.inRect(button.rect)){
                    slideToButton(button,info.id,invalidate)
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
    fun dispatchDownEvent(info: TouchInfo,invalidate:(Button)->Unit):Boolean {
        if(!info.inRect(rect)){
            return false
        }
        for(button in buttons){
            if(info.inRect(button.rect)){
                downButtons.add(button)
                button.press(info.id,invalidate)
                lastTouchPoint = Point(info.pointX,info.pointY)
                return true
            }
        }
        return false
    }
    fun dispatchUpEvent(info: TouchInfo,invalidate:(Button)->Unit) {
        downButtons.removeAll {
            (info.id == it.pointerId).apply {
                if(this){
                    it.up(invalidate)
                    lastTouchPoint = null
                }
            }
        }
    }
}