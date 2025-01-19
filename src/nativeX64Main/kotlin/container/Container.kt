package container

import button.Button
import buttonGroup.Group
import error.emptyContainerError
import kotlinx.cinterop.ExperimentalForeignApi
import touch.TouchReceiver

@OptIn(ExperimentalForeignApi::class)
class Container(
    val groups:List<Group>,
    val alpha:UByte = 128u,
):TouchReceiver{
    init {
        if(groups.isEmpty()) emptyContainerError()
    }
    override fun down(info: TouchReceiver.TouchEvent):Boolean {
        groups.firstOrNull{
            it.dispatchDownEvent(info,invalidate)
        }?.let {
            activePointers[info.id] = it
            return true
        }
        return false
    }

    override fun up(info: TouchReceiver.TouchEvent):Boolean {
        activePointers[info.id]?.let{
            it.dispatchUpEvent(info,invalidate)
            activePointers.remove(info.id)
        } ?: return false
        return true
    }

    override fun move(info: TouchReceiver.TouchEvent):Boolean {
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