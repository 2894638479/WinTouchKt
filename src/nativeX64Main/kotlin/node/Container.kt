package node

import dsl.mutStateNull
import dsl.mutStateOf
import node.Button.ButtonSerializer
import error.wrapExceptionName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import logger.info
import logger.warning
import platform.posix.exit
import sendInput.KeyHandler
import touch.TouchReceiver
import window.buttonsLayeredWindow
import wrapper.Hwnd
import wrapper.SerializerWrapper

@Serializable(with = Container.ContainerSerializer::class)
class Container :TouchReceiver, NodeWithChild<Group>(){
    val groups get() = children
    private val buttonSequence = sequence { groups.forEach { it.buttons.forEach { yield(it) } } }
    var isEditMode by mutStateOf(false)
    val drawScope = DrawScope(buttonSequence,buttonsLayeredWindow("container_window"))
        .also { setHwndContainer(it.hwnd,this) }
    val keyHandler = KeyHandler({ drawScope.run { showStatus = !showStatus } }) { info("exit pressed");exit(0) }
    init {
        context = Context(drawScope,keyHandler)
        extract { context }.listen { error("should not modify context of container") }
    }
    class Context(
        val drawScope: DrawScope,
        val keyHandler: KeyHandler
    )
    var alpha by mutStateNull<UByte>(true) { context?.run { drawScope.alpha = (it ?: 128u) } ?: error("context is null") }

    override fun down(event: TouchReceiver.TouchEvent):Boolean {
        groups.firstOrNull {
            wrapExceptionName("dispatcher down error"){
                it.touchDispatcher.down(event)
            }
        }?.let {
            activePointers[event.id] = it
            return true
        }
        return false
    }

    override fun up(event: TouchReceiver.TouchEvent):Boolean {
        activePointers[event.id]?.let{
            wrapExceptionName("dispatcher up error") {
                it.touchDispatcher.up(event)
            }
            activePointers.remove(event.id)
        } ?: return false
        return true
    }

    override fun move(event: TouchReceiver.TouchEvent):Boolean {
        wrapExceptionName("dispatcher move error") {
            activePointers[event.id]?.touchDispatcher?.move(event) ?: return false
        }
        return true
    }


    private val activePointers = mutableMapOf<UInt, Group>()
    override fun destroy(){
        removeContainer(this)
        context?.drawScope?.destroy() ?: error("drawScope is null")
        super.destroy()
    }
    companion object {
        private val hwndContainer = HashMap<Hwnd, Container>(10)
        fun setHwndContainer(hwnd: Hwnd, container: Container){
            hwndContainer[hwnd] = container
        }
        fun hwndContainer(hwnd: Hwnd) = hwndContainer[hwnd]
        fun removeContainer(container: Container) {
            val key = hwndContainer.firstNotNullOf { (k,v) -> k.takeIf { v == container } }
            hwndContainer.remove(key)
        }
    }

    object ContainerSerializer : SerializerWrapper<Container,ContainerSerializer.Descriptor>("Container",Descriptor) {
        object Descriptor : Node.Descriptor<Container>() {
            val alpha = "alpha" from {alpha}
            val groups = "groups" from {groups.list}
        }
        override fun Descriptor.generate(): Container {
            return Container().also {
                it.addNodeInfo()
                it.alpha = alpha.nullable
                it.groups += groups.nonNull
            }
        }
    }

    override fun toString() = "Container${Json.encodeToString(this)}"
}