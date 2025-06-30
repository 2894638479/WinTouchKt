package button

import button.ButtonStyle.Companion.default
import button.ButtonStyle.Companion.defaultPressed
import error.infoBox
import error.nullPtrError
import json.RectJson
import json.RoundJson
import json.RoundedRectJson
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import mainContainer
import sendInput.KEYEVENT_DOWN
import sendInput.KEYEVENT_UP
import sendInput.sendAllKeyEvent
import sendInput.sendAllKeyEventFilter

@Serializable(with = Button.ButtonSerializer::class)
class Button(
    val name:String,
    val key:Set<UByte>,
    val shapeOrig:Shape,
){
    private var shapeActual:Shape? = null
    fun setShapeActual(offset:Point?,scale:Float){
        shapeActual = shapeOrig.run { if(offset == null) this else offset(offset) }.rescaled(scale)
    }
    val shape get() = shapeActual ?: nullPtrError()
    var style: ButtonStyle? = null
    var stylePressed: ButtonStyle? = null
    var count = 0u
        private set
    inline val pressed get() = count != 0u
    fun findContainer() = mainContainer
    @ExperimentalForeignApi
    fun findGroup() = mainContainer.groups.find { it.buttons.contains(this) } ?: error("button not found group")
    @OptIn(ExperimentalForeignApi::class)
    fun findCorrectStyle():ButtonStyle{
        val group = findGroup()
        val container = findContainer()
        val styleList:List<ButtonStyle?> = if(pressed) listOf(stylePressed,group.stylePressed,container.stylePressed, defaultPressed)
        else listOf(style,group.style,container.style, default)
        return styleList.filterNotNull().let {
            it.firstOrNull()?.apply { parents = it.drop(1) } ?: nullPtrError()
        }
    }
    fun down(invalidate:(Button)->Unit){
        sendAllKeyEvent(KEYEVENT_DOWN)
        count++
        invalidate(this)
    }
    fun up(invalidate:(Button)->Unit){
        sendAllKeyEvent(KEYEVENT_UP)
        count--
        invalidate(this)
    }
    fun downNoKey(invalidate:(Button)->Unit){
        count++
        invalidate(this)
    }
    fun upNoKey(invalidate: (Button) -> Unit){
        count--
        invalidate(this)
    }
    fun slideDown(invalidate:(Button)->Unit,filter:(UByte)->Boolean){
        sendAllKeyEventFilter(KEYEVENT_DOWN,filter)
        count++
        invalidate(this)
    }
    fun slideUp(invalidate:(Button)->Unit,filter:(UByte)->Boolean){
        sendAllKeyEventFilter(KEYEVENT_UP,filter)
        count--
        invalidate(this)
    }
    fun inArea(x:Float,y:Float) = shape.containPoint(x,y)

    object ButtonSerializer : KSerializer<Button> {
        override val descriptor = buildClassSerialDescriptor("Button"){
            element<String>("name")
            element<Set<UByte>>("key")
            element<RectJson>("rect")
            element<RoundedRectJson>("roundedRect")
            element<RoundJson>("round")
            element<ButtonStyle>("style")
            element<ButtonStyle>("stylePressed")
        }
        override fun deserialize(decoder: Decoder): Button {
            var name:String? = null
            var key:Set<UByte>? = null
            var rectJson:RectJson? = null
            var roundedRectJson:RoundedRectJson? = null
            var roundJson:RoundJson? = null
            var style:ButtonStyle? = null
            var stylePressed:ButtonStyle? = null
            println("button1")
            decoder.decodeStructure(descriptor) {
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> name = decodeStringElement(descriptor,index)
                        1 -> key = decodeSerializableElement(descriptor,index,SetSerializer(UByte.serializer()))
                        2 -> rectJson = decodeSerializableElement(descriptor,index,RectJson.serializer())
                        3 -> roundedRectJson = decodeSerializableElement(descriptor,index,RoundedRectJson.serializer())
                        4 -> roundJson = decodeSerializableElement(descriptor,index,RoundJson.serializer())
                        5 -> style = decodeSerializableElement(descriptor,index,ButtonStyle.serializer())
                        6 -> stylePressed = decodeSerializableElement(descriptor,index,ButtonStyle.serializer())
                        CompositeDecoder.DECODE_DONE -> break
                        else -> error("Unexpected index: $index")
                    }
                }
            }
            var shapeCount = 0
            rectJson?.let { shapeCount++ }
            roundJson?.let { shapeCount++ }
            roundedRectJson?.let { shapeCount++ }
            if(shapeCount != 1) error("每个按钮需要选择 rect round roundedRect 三种形状中的一种")
            val shape = rectJson?.toRect()
                ?: roundedRectJson?.toRoundedRect()
                ?: roundJson?.toRound()
                ?: nullPtrError()
            println("button2")
            return Button(
                name ?: error("button has no name"),key ?: error("button has no key"),shape
            ).also { it.style = style;it.stylePressed = stylePressed;println("button3") }
        }
        override fun serialize(encoder: Encoder, value: Button) = value.run {
            encoder.encodeStructure(descriptor){
                encodeStringElement(descriptor, 0,name)
                encodeSerializableElement(descriptor,1, SetSerializer(UByte.serializer()),key)
                if(shapeOrig is Rect) encodeSerializableElement(descriptor,2,RectJson.serializer(),shapeOrig.run { RectJson(x,y,w,h) })
                else if(shapeOrig is RoundedRect) encodeSerializableElement(descriptor,3,
                    RoundedRectJson.serializer(),shapeOrig.run { RoundedRectJson(x,y,w,h,r) })
                else if(shapeOrig is Round) encodeSerializableElement(descriptor,4,RoundJson.serializer(),shapeOrig.run { RoundJson(x,y,r) })
                else error("shape type logic error")
                style?.let { encodeSerializableElement(descriptor,5,ButtonStyle.serializer(),it) }
                stylePressed?.let { encodeSerializableElement(descriptor,6,ButtonStyle.serializer(),it) }
            }
        }
    }
}