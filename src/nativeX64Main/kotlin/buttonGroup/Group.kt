package buttonGroup

import button.Button
import button.Button.ButtonSerializer
import button.ButtonStyle
import button.Point
import button.Rect
import error.groupTypeError
import error.nullPtrError
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import touch.TouchReceiver

@ExperimentalForeignApi
@Serializable(with = Group.GroupSerializer::class)
abstract class Group(
    val buttons: List<Button>,
){
    var style: ButtonStyle? = null
        private set
    var stylePressed: ButtonStyle? = null
        private set
    var offset:Point? = null
        private set
    var rect:Rect? = null
        private set
    fun updateScale(scale:Float){
        buttons.forEach {
            it.setShapeActual(offset,scale)
        }
        rect = buttons.getOrNull(0)?.run {
            shape.outerRect.toMutableRect().apply {
                buttons.forEach { this += it.shape.outerRect }
            }.toRect()
        }
    }
    protected val pointers = mutableMapOf<UInt,MutableList<Button>>()
    abstract fun dispatchMoveEvent(event: TouchReceiver.TouchEvent, invalidate:(Button)->Unit)
    abstract fun dispatchDownEvent(event: TouchReceiver.TouchEvent, invalidate:(Button)->Unit):Boolean
    open fun dispatchUpEvent(event: TouchReceiver.TouchEvent, invalidate:(Button)->Unit){
        pointers[event.id]?.forEach { it.up(invalidate) } ?: nullPtrError()
        pointers.remove(event.id)
    }

    protected inline fun firstOrNull(x: Float, y: Float):Button? {
        if (rect?.containPoint(x,y) != true) return null
        return buttons.firstOrNull { it.inArea(x,y) }
    }
    protected inline fun alreadyDown(button: Button,id: UInt):Boolean {
        return pointers[id]?.contains(button) ?: false
    }

    protected inline fun slide(toUp:Button, toDown:Button, pressedButtons:MutableList<Button>, noinline invalidate: (Button) -> Unit){
        toUp.slideUp(invalidate) { !toDown.key.contains(it) }
        toDown.slideDown(invalidate) { !toUp.key.contains(it) }
        pressedButtons.remove(toUp)
        pressedButtons.add(toDown)
    }
    init {
        println("cons 2")
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
            println("group1")
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
            println("group1.5  $type ${buttons.size}")
            val group: Group = when(type.toInt()){
                0 -> NormalGroup(buttons)
                1 -> SlideGroup(buttons, slideCount)
                2 -> HoldSlideGroup(buttons, holdIndex)
                3 -> HoldGroup(buttons)
                4 -> HoldGroupDoubleClk(buttons, ms)
                7 -> TouchPadGroup(buttons, sensitivity, ms)
                8 -> MouseGroup(buttons, sensitivity)
                9 -> ScrollGroup(buttons, sensitivity)
                else -> groupTypeError(type)
            }
            println("group2")
            return group.also {
                it.offset = offset
                it.style = style
                it.stylePressed = stylePressed
            }
        }
        override fun serialize(encoder: Encoder, value: Group) = value.run {
            encoder.encodeStructure(descriptor){
                offset?.let { encodeSerializableElement(descriptor,1,Point.serializer(),it) }
                encodeSerializableElement(descriptor,2, ListSerializer(Button.serializer()),buttons)
                style?.let { encodeSerializableElement(ButtonSerializer.descriptor,7,ButtonStyle.serializer(),it) }
                stylePressed?.let { encodeSerializableElement(ButtonSerializer.descriptor,8,ButtonStyle.serializer(),it) }
                val type = when(this@run){
                    is SlideGroup -> {
                        encodeSerializableElement(descriptor, 4, UInt.serializer(), slideCount)
                        1
                    }
                    is HoldSlideGroup -> {
                        encodeIntElement(descriptor, 6, holdIndex)
                        2
                    }
                    is HoldGroup -> 3
                    is HoldGroupDoubleClk -> {
                        encodeSerializableElement(descriptor,5,ULong.serializer(),ms)
                        4
                    }
                    is TouchPadGroup -> {
                        encodeFloatElement(descriptor,3,sensitivity)
                        encodeSerializableElement(descriptor,5,ULong.serializer(),ms)
                        7
                    }
                    is MouseGroup -> {
                        encodeFloatElement(descriptor,3,sensitivity)
                        8
                    }
                    is ScrollGroup -> {
                        encodeFloatElement(descriptor,3,sensitivity)
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