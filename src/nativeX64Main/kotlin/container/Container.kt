package container

import button.Button
import button.HasButtonConfigs
import buttonGroup.ButtonGroup
import draw.Color
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import libs.Clib.TouchInfo
import touch.TouchReceiver

@OptIn(ExperimentalForeignApi::class)
@Serializable
class Container(
    val groups:List<ButtonGroup>,
    override var textColor:Color? = null,
    override var textColorPressed: Color? = null,
    override var color:Color? = null,
    override var colorPressed:Color? = null,
    override var textSize:Byte? = null,
    val alpha:UByte = 128u
):TouchReceiver,HasButtonConfigs{
    init {
        if(groups.isEmpty()) error("creating an empty group")
        groups.forEach {
            it.copyConfig(this)
        }
    }
    override fun down(info: TouchInfo):Boolean {
        groups.firstOrNull{
            it.dispatchDownEvent(info,invalidate)
        }?.let {
            activePointers[info.id] = it
            return true
        }
        return false
    }

    override fun up(info: TouchInfo):Boolean {
        activePointers[info.id]?.let{
            it.dispatchUpEvent(info,invalidate)
            activePointers.remove(info.id)
        } ?: return false
        return true
    }

    override fun move(info: TouchInfo):Boolean {
        activePointers[info.id]?.dispatchMoveEvent(info,invalidate) ?: return false
        return true
    }

    @Transient val activePointers = mutableMapOf<UInt, ButtonGroup>()
    inline fun forEachButton(block:(Button)->Unit){
        for(group in groups){
            for(button in group.buttons){
                block(button)
            }
        }
    }

    @Transient var invalidate: (Button) -> Unit = {  }
}