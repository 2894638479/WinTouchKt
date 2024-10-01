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
    override fun down(info: TouchInfo) {
        groups.firstOrNull{
            it.dispatchDownEvent(info)
        }?.let {
            activePointers[info.id] = it
        }
    }

    override fun up(info: TouchInfo) {
        activePointers[info.id]?.let{
            it.dispatchUpEvent(info)
            activePointers.remove(info.id)
        }
    }

    override fun move(info: TouchInfo) {
        activePointers[info.id]?.dispatchMoveEvent(info)
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