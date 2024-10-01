package buttonGroup

import button.Button
import button.Point
import button.inRect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import libs.Clib.TouchInfo
import kotlinx.serialization.json.Json

@Serializable
@OptIn(ExperimentalForeignApi::class)
class ButtonGroup(
    val buttons: List<Button>,
    val typeValue:Int,
    private val offset: Point
) {
    init {
        if(buttons.isEmpty()) throw IllegalArgumentException("creating an empty buttonGroup")
    }
    enum class GroupType{
        Static,Slide
    }
    val type:GroupType get() {
        return if (typeValue > 0) GroupType.Slide
        else if (typeValue == 0) GroupType.Static
        else throw IllegalStateException("unknown group type: $typeValue")
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
            downButtons.add(button)
            button.slidePress(pointer) { !isKeyPressed(it) }
            if (downButtons.size > typeValue) {
                downButtons[0].slideUp { !button.key.contains(it) }
                downButtons.removeAt(0)
            }
        } else {
            if(button !== downButtons.last()){
                downButtons.remove(button)
                downButtons.add(button)
            }
        }
    }
    fun dispatchMoveEvent(info: TouchInfo) = when(type) {
        GroupType.Static -> {  }
        GroupType.Slide -> {
            for(button in buttons){
                if(info.inRect(button.rect)){
                    slideToButton(button,info.id)
                }
            }
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
                }
            }
        }
    }
}