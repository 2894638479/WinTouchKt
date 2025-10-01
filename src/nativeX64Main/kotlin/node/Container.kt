package node

import dsl.mutStateNull
import dsl.mutStateOf
import error.wrapExceptionName
import geometry.Point
import geometry.plus
import gui.window.openExitWindow
import json
import VERSION
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger.infoBox
import logger.warningBox
import sendInput.KeyHandler
import touch.TouchReceiver
import window.buttonsLayeredWindow
import wrapper.Hwnd
import wrapper.SerializerWrapper
import writeFile

@Serializable(with = Container.ContainerSerializer::class)
class Container :TouchReceiver, NodeWithChild<Group>(){
    var filePath = ""
    override val defaultName: String get() = filePath.substringAfterLast("\\")
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
    var selected by mutStateOf<Node?>(null)
    val drawScope = DrawScope(buttonSequence,{selected?.displayOffset},buttonsLayeredWindow("container_window"))
        .also { setHwndContainer(it.hwnd,this) }
    val keyHandler = KeyHandler({ drawScope.run { showStatus = !showStatus } }) { openExitWindow(this) }
    init {
        extract { context }.value = Context(drawScope,keyHandler)
        extract { context }.listen { error("should not modify context of container") }
        extract { selected }.listen { drawScope.reDraw = true }
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
        val receiver = if(status == Status.DRAG_CONTAINER) object : TouchReceiver {
            var lastTouchPoint: Point? = null
            val pointers = mutableListOf<UInt>()
            override fun down(event: TouchReceiver.TouchEvent): Boolean {
                pointers += event.id
                lastTouchPoint = event.point
                return true
            }
            override fun move(event: TouchReceiver.TouchEvent): Boolean {
                if(event.id !in pointers) return false
                offset += event.point - (lastTouchPoint ?: error("no lastTouchPoint"))
                lastTouchPoint = event.point
                return true
            }
            override fun up(event: TouchReceiver.TouchEvent): Boolean {
                return pointers.remove(event.id)
            }
        }.apply { down(event) } else groups.firstOrNull {
            wrapExceptionName("dispatcher down error"){
                it.touchReceiver?.down(event) == true
            }
        }?.touchReceiver

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
        super<NodeWithChild>.destroy()
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

    fun saveToFile(path:String = filePath): Boolean{
        val success = writeFile(path,json.encodeToString(this))
        if(success) infoBox("配置已保存到$path")
        else warningBox("配置无法保存到$path")
        return success
    }

    fun closeConfig(){
        status = Status.NORMAL
        selected = null
        drawScope.showStatus = true
        drawScope.reDraw = true
    }

    object ContainerSerializer : SerializerWrapper<Container,ContainerSerializer.Descriptor>("Container",Descriptor) {
        object Descriptor : Node.Descriptor<Container>() {
            val version = "version" from {VERSION}
            val alpha = "alpha" from {alpha}
            val groups = "groups" from {groups}
        }
        override fun Descriptor.generate(): Container {
            if(version.nullable != VERSION){
                warningBox("配置文件版本：${version.nullable}，当前软件版本：$VERSION，可能造成配置内容丢失。")
            }
            return Container().also {
                it.addNodeInfo()
                it.alpha = alpha.nullable
                it.children += groups.nonNull
            }
        }
    }

    override fun toString() = "Container${Json.encodeToString(this)}"
}