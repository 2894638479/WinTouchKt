package node

import dsl.mutStateNull
import dsl.mutStateOf
import error.wrapExceptionName
import group.GroupTouchDispatcher
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger.info
import platform.posix.exit
import sendInput.KeyHandler
import touch.TouchReceiver
import window.buttonsLayeredWindow
import wrapper.Hwnd
import wrapper.SerializerWrapper
import geometry.plus

@Serializable(with = Container.ContainerSerializer::class)
class Container :TouchReceiver, NodeWithChild<Group>(){
    enum class Status{
        NORMAL,DRAG_BUTTON,DRAG_GROUP,DRAG_CONTAINER,SELECT_BUTTON,SELECT_GROUP;
        val str get() = when(this){
            NORMAL -> "正常模式"
            DRAG_BUTTON -> "拖拽模式"
            DRAG_GROUP -> "拖拽整组"
            DRAG_CONTAINER -> "拖拽全部"
            SELECT_BUTTON -> "选择模式"
            SELECT_GROUP -> "选择分组"
        }
    }
    val groups by children
    private val buttonSequence = sequence { groups.forEach { it.buttons.forEach { yield(it) } } }
    var status by mutStateOf(Status.NORMAL)
    var selected by mutStateOf<Node?>(this)
    val drawScope = DrawScope(buttonSequence,buttonsLayeredWindow("container_window"))
        .also { setHwndContainer(it.hwnd,this) }
    val keyHandler = KeyHandler({ drawScope.run { showStatus = !showStatus } }) { info("exit pressed");exit(0) }
    init {
        extract { context }.value = Context(drawScope,keyHandler)
        extract { context }.listen { error("should not modify context of container") }
    }
    class Context(
        val drawScope: DrawScope,
        val keyHandler: KeyHandler
    )
    var alpha by mutStateNull<UByte>().apply {
        listen(true) {
            context?.run { drawScope.alpha = (it ?: 128u) } ?: error("context is null")
        }
    }

    override fun down(event: TouchReceiver.TouchEvent):Boolean {
        val receiver = if(status == Status.DRAG_CONTAINER)object : TouchReceiver{
            override fun move(event: TouchReceiver.TouchEvent): Boolean {
                offset += event.point / displayScale
                return true
            }
        } else groups.firstOrNull {
            wrapExceptionName("dispatcher down error"){
                it.touchDispatcher?.down(event) == true
            }
        }?.touchDispatcher

        activePointers[event.id] = receiver ?: return false
        return true
    }

    override fun up(event: TouchReceiver.TouchEvent):Boolean {
        val dispatcher = activePointers[event.id] ?: return false
        if(!dispatcher.valid) return false
        wrapExceptionName("dispatcher up error") {
            dispatcher.up(event)
        }
        return true
    }

    override fun move(event: TouchReceiver.TouchEvent):Boolean {
        val dispatcher = activePointers[event.id] ?: return false
        if(!dispatcher.valid) return false
        wrapExceptionName("dispatcher move error") {
            dispatcher.move(event)
        }
        return true
    }


    private val activePointers = mutableMapOf<UInt, TouchReceiver>()
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
            val groups = "groups" from {groups}
        }
        override fun Descriptor.generate(): Container {
            return Container().also {
                it.addNodeInfo()
                it.alpha = alpha.nullable
                it.children += groups.nonNull
            }
        }
    }

    override fun toString() = "Container${Json.encodeToString(this)}"
}