package container

import button.Button
import button.Button.ButtonSerializer
import button.ButtonStyle
import buttonGroup.Group
import draw.DrawScope
import error.wrapExceptionName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import logger.info
import platform.posix.exit
import sendInput.KeyHandler
import touch.TouchReceiver
import window.buttonsLayeredWindow
import wrapper.Hwnd

@Serializable(with = Container.ContainerSerializer::class)
class Container :TouchReceiver,NodeWithChild<Group>(){
    override val parent get() = null
    val groups:MutableList<Group> = mutableListOf()
    override val children get() = groups
    private val buttonSequence = sequence { groups.forEach { it.buttons.forEach { yield(it) } } }
    fun addGroup(group:Group){
        group.parent = this
        children += group
    }
    fun removeGroup(group: Group){
        if(!children.remove(group)) error("remove group failed")
        group.parent = null
    }
    var isEditMode = false
        set(value) {
            field = value
            if(value) {

            } else {

            }
        }

    val drawScope = DrawScope(buttonSequence,buttonsLayeredWindow("container_window"))
        .also { setHwndContainer(it.hwnd,this) }
    val keyHandler = KeyHandler({ drawScope.run { showStatus = !showStatus } }) { info("exit pressed");exit(0) }
    val touchScope = ButtonTouchScope(keyHandler,drawScope)
    class ButtonTouchScope(
        val keyHandler: KeyHandler,
        private val drawScope: DrawScope
    ) {
        fun toDraw(button:Button){ drawScope.toDraw(button) }
        fun toErase(button:Button){ drawScope.toErase(button) }
        fun toDrawAll(){ drawScope.toDrawAll() }
    }
    var alpha:UByte? = null
        set(value) {
            field = value
            drawScope.alpha = value ?: 128u
        }
    override fun findDrawScopeCache() = drawScope.cache
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
    fun destroy(){
        removeContainer(this)
        drawScope.destroy()
    }
    companion object {
        private val hwndContainer = HashMap<Hwnd,Container>(10)
        fun setHwndContainer(hwnd: Hwnd, container: Container){
            hwndContainer[hwnd] = container
        }
        fun hwndContainer(hwnd: Hwnd) = hwndContainer[hwnd]
        fun removeContainer(container: Container) {
            val key = hwndContainer.firstNotNullOf { (k,v) -> k.takeIf { v == container } }
            hwndContainer.remove(key)
        }
    }

    object ContainerSerializer : KSerializer<Container> {
        override val descriptor = buildClassSerialDescriptor("Container"){
            element<UByte>("alpha")
            element<Float>("scale")
            element<List<Group>>("groups")
            element<ButtonStyle>("style")
            element<ButtonStyle>("stylePressed")
        }
        override fun deserialize(decoder: Decoder): Container {
            var alpha:UByte = 128u
            var scale:Float = 1f
            var groups:List<Group> = emptyList()
            var style: ButtonStyle? = null
            var stylePressed: ButtonStyle? = null
            decoder.decodeStructure(descriptor) {
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> alpha = decodeSerializableElement(descriptor,index,UByte.serializer())
                        1 -> scale = decodeFloatElement(descriptor, index)
                        2 -> groups = decodeSerializableElement(descriptor,index, ListSerializer(Group.serializer()))
                        3 -> style = decodeSerializableElement(descriptor,index,ButtonStyle.serializer())
                        4 -> stylePressed = decodeSerializableElement(descriptor,index,ButtonStyle.serializer())
                        CompositeDecoder.DECODE_DONE -> break
                        else -> error("Unexpected index: $index")
                    }
                }
            }
            return Container().also {
                it.scale = scale
                it.alpha = alpha
                it.style = style
                it.stylePressed = stylePressed
                groups.forEach(it::addGroup)
            }
        }
        override fun serialize(encoder: Encoder, value: Container) = value.run {
            encoder.encodeStructure(descriptor){
                alpha?.let{ encodeSerializableElement(descriptor, 0,UByte.serializer(),it) }
                scale?.let { encodeFloatElement(descriptor,1, it) }
                encodeSerializableElement(descriptor,2, ListSerializer(Group.serializer()),groups)
                style?.let { encodeSerializableElement(ButtonSerializer.descriptor,3,ButtonStyle.serializer(),it) }
                stylePressed?.let { encodeSerializableElement(ButtonSerializer.descriptor,4,ButtonStyle.serializer(),it) }
            }
        }
    }
}