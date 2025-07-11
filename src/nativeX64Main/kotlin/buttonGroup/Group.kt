package buttonGroup

import button.Button
import button.Button.ButtonSerializer
import button.ButtonStyle
import button.Point
import container.Container
import container.NodeWithChild
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import touch.TouchReceiver
import wrapper.WeakRefDel

@Serializable(with = Group.GroupSerializer::class)
class Group(
    createDispatcher:(Group)-> GroupTouchDispatcher
): NodeWithChild<Button>() {
    val buttons: MutableList<Button> = mutableListOf()
    var touchDispatcher = createDispatcher(this)
        private set
    val editModeTouchDispatcher = object :GroupTouchDispatcher(this) {
        override fun down(event: TouchReceiver.TouchEvent):Boolean {
            return firstOrNull(event.x,event.y)?.let {
                pointers[event.id] = mutableListOf(it)
            } != null
        }
        override fun move(event: TouchReceiver.TouchEvent): Boolean {
            val scope = container.drawScope
            val button = pointers[event.id]?.getOrNull(0) ?: error("pointer id not down")
            scope.toErase(button)
            val offset = Point(event.x,event.y)
            button.offset = button.offset?.plus(offset) ?: offset
            scope.toDrawAll()
            return true
        }
        override fun up(event: TouchReceiver.TouchEvent): Boolean {
            pointers.remove(event.id) ?: error("pointer id not down")
            return true
        }
    }

    override var parent by WeakRefDel<Container>()
    inline val container get() = parent ?: error("group parent is null")
    override val children get() = buttons
    fun addButton(button: Button){
        button.parent = this
        buttons += button
    }
    fun removeButton(button: Button){
        if(!buttons.remove(button)) error("remove button error")
        button.parent = null
    }

    object GroupSerializer : KSerializer<Group> {
        override val descriptor = buildClassSerialDescriptor("Group"){
            element<UByte>("type")
            element<Point>("offset")
            element<List<Button>>("buttons")
            element<Float>("sensitivity")
            element<UInt>("slideCount")
            element<ULong>("ms")
            element<Int>("holdIndex")
            element<ButtonStyle>("style")
            element<ButtonStyle>("stylePressed")
        }
        override fun deserialize(decoder: Decoder): Group {
            var type: UByte = 0u
            var offset: Point? = null
            var buttons: List<Button> = emptyList()
            var sensitivity:Float = 1f
            var slideCount:UInt = 1u
            var ms:ULong = 300uL
            var holdIndex:Int = 0
            var style: ButtonStyle? = null
            var stylePressed: ButtonStyle? = null
            decoder.decodeStructure(descriptor) {
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> type = decodeSerializableElement(descriptor,index,UByte.serializer())
                        1 -> offset = decodeSerializableElement(descriptor,index,Point.serializer())
                        2 -> buttons = decodeSerializableElement(descriptor,index, ListSerializer(Button.serializer()))
                        3 -> sensitivity = decodeFloatElement(descriptor,index)
                        4 -> slideCount = decodeSerializableElement(descriptor,index,UInt.serializer())
                        5 -> ms = decodeSerializableElement(descriptor,index,ULong.serializer())
                        6 -> holdIndex = decodeIntElement(descriptor,index)
                        7 -> style = decodeSerializableElement(ButtonSerializer.descriptor,index,ButtonStyle.serializer())
                        8 -> stylePressed = decodeSerializableElement(ButtonSerializer.descriptor,index,ButtonStyle.serializer())
                        CompositeDecoder.DECODE_DONE -> break
                        else -> error("Unexpected index: $index")
                    }
                }
            }
            return Group {
                when(type.toInt()){
                    0 -> NormalGroup(it)
                    1 -> SlideGroup(it, slideCount)
                    2 -> HoldSlideGroup(it, holdIndex)
                    3 -> HoldGroup(it)
                    4 -> HoldGroupDoubleClk(it, ms)
                    7 -> TouchPadGroup(it, sensitivity, ms)
                    8 -> MouseGroup(it, sensitivity)
                    9 -> ScrollGroup(it, sensitivity)
                    else -> error("unknown group type $type")
                }
            }.also {
                it.offset = offset
                it.style = style
                it.stylePressed = stylePressed
                buttons.forEach(it::addButton)
            }
        }
        override fun serialize(encoder: Encoder, value: Group) = value.run {
            encoder.encodeStructure(descriptor){
                offset?.let { encodeSerializableElement(descriptor,1,Point.serializer(),it) }
                encodeSerializableElement(descriptor,2, ListSerializer(Button.serializer()),buttons)
                style?.let { encodeSerializableElement(ButtonSerializer.descriptor,7,ButtonStyle.serializer(),it) }
                stylePressed?.let { encodeSerializableElement(ButtonSerializer.descriptor,8,ButtonStyle.serializer(),it) }
                val td = touchDispatcher
                val type = when(td){
                    is SlideGroup -> {
                        encodeSerializableElement(descriptor, 4, UInt.serializer(), td.slideCount)
                        1
                    }
                    is HoldSlideGroup -> {
                        encodeIntElement(descriptor, 6, td.holdIndex)
                        2
                    }
                    is HoldGroup -> 3
                    is HoldGroupDoubleClk -> {
                        encodeSerializableElement(descriptor,5,ULong.serializer(),td.ms)
                        4
                    }
                    is TouchPadGroup -> {
                        encodeFloatElement(descriptor,3,td.sensitivity)
                        encodeSerializableElement(descriptor,5,ULong.serializer(),td.ms)
                        7
                    }
                    is MouseGroup -> {
                        encodeFloatElement(descriptor,3,td.sensitivity)
                        8
                    }
                    is ScrollGroup -> {
                        encodeFloatElement(descriptor,3,td.sensitivity)
                        9
                    }
                    is NormalGroup -> 0
                    else -> error("unknown group type")
                }
                encodeSerializableElement(descriptor,0,UByte.serializer(),type.toUByte())
            }
        }
    }
}