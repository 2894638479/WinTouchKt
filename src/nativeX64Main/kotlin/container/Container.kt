package container

import button.Button
import button.HasButtonConfigs
import buttonGroup.Group
import draw.Color
import error.emptyContainerError
import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.TouchInfo
import touch.TouchReceiver

@OptIn(ExperimentalForeignApi::class)
class Container(
    val groups:List<Group>,
    val alpha:UByte = 128u,
    override var textColor:Color? = null,
    override var textColorPressed: Color? = null,
    override var color:Color? = null,
    override var colorPressed:Color? = null,
    override var textSize:Byte? = null
):TouchReceiver,HasButtonConfigs{
    init {
        if(groups.isEmpty()) emptyContainerError()
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

    private val activePointers = mutableMapOf<UInt, Group>()
    inline fun forEachButton(block:(Button)->Unit){
        for(group in groups){
            for(button in group.buttons){
                block(button)
            }
        }
    }

    var invalidate: (Button) -> Unit = {  }
}