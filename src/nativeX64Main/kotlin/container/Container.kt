package container

import buttonGroup.ButtonGroup
import button.Button
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import libs.Clib.TouchInfo
import touch.TouchReceiver

@OptIn(ExperimentalForeignApi::class)
@Serializable
class Container(val groups:List<ButtonGroup>):TouchReceiver{
    override fun down(info: TouchInfo):Boolean {
        groups.firstOrNull{
            it.dispatchDownEvent(info)
        }?.let {
            activePointers[info.id] = it
            return true
        }
        return false
    }

    override fun up(info: TouchInfo):Boolean {
        activePointers[info.id]?.let{
            it.dispatchUpEvent(info)
            activePointers.remove(info.id)
        } ?: return false
        return true
    }

    override fun move(info: TouchInfo):Boolean {
        activePointers[info.id]?.dispatchMoveEvent(info) ?: return false
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
}