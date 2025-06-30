package container

import button.Button
import button.Button.ButtonSerializer
import button.ButtonStyle
import buttonGroup.Group
import error.emptyContainerError
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import touch.TouchReceiver

@Serializable(with = Container.ContainerSerializer::class)
@OptIn(ExperimentalForeignApi::class)
class Container(
    groups:List<Group>,
    alpha:UByte,
    scale:Float
):TouchReceiver{
    val groups = groups.toMutableList()
    val alpha = alpha
    var scale = scale
        set(value) {
            field = value
            updateButtonShape()
        }
    var style: ButtonStyle? = null
    var stylePressed: ButtonStyle? = null
    fun updateButtonShape(){
        groups.forEach {
            it.updateScale(scale)
        }
    }
    init {
        if(groups.isEmpty()) emptyContainerError()
        updateButtonShape()
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
            return Container(groups, alpha,scale).also {
                it.style = style
                it.stylePressed = stylePressed
            }
        }
        override fun serialize(encoder: Encoder, value: Container) = value.run {
            encoder.encodeStructure(descriptor){
                encodeSerializableElement(descriptor, 0,UByte.serializer(),alpha)
                encodeFloatElement(descriptor,1,scale)
                encodeSerializableElement(descriptor,2, ListSerializer(Group.serializer()),groups)
                style?.let { encodeSerializableElement(ButtonSerializer.descriptor,3,ButtonStyle.serializer(),it) }
                stylePressed?.let { encodeSerializableElement(ButtonSerializer.descriptor,4,ButtonStyle.serializer(),it) }
            }
        }
    }
}