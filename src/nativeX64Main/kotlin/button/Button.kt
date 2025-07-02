package button

import container.Node
import error.nullPtrError
import json.RectJson
import json.RoundJson
import json.RoundedRectJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import sendInput.KEYEVENT_DOWN
import sendInput.KEYEVENT_UP
import sendInput.sendAllKeyEvent
import sendInput.sendAllKeyEventFilter

@Serializable(with = Button.ButtonSerializer::class)
class Button(
    var key:Set<UByte>,
    shapeOrig:Shape,
):Node(){
    var shapeOrig = shapeOrig
        set(value) { field = value.apply { cache.invalidate() } }
    val shape get() = cache.shape ?: nullPtrError()
    override fun calOuterRect() = shape.outerRect
    override fun calCurrentShape() = cache.applyShape(shapeOrig)
    var count = 0u
        private set
    inline val pressed get() = count != 0u
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
            return Button(key ?: error("button has no key"),shape).also {
                it.style = style
                it.stylePressed = stylePressed
                it.name = name ?: error("button has no name")
            }
        }
        override fun serialize(encoder: Encoder, value: Button) = value.run {
            encoder.encodeStructure(descriptor){
                name?.let { encodeStringElement(descriptor, 0, it) }
                encodeSerializableElement(descriptor,1, SetSerializer(UByte.serializer()),key)
                val s = shapeOrig
                if(s is Rect) encodeSerializableElement(descriptor,2,RectJson.serializer(),s.run { RectJson(x,y,w,h) })
                else if(s is RoundedRect) encodeSerializableElement(descriptor,3, RoundedRectJson.serializer(),s.run { RoundedRectJson(x,y,w,h,r) })
                else if(s is Round) encodeSerializableElement(descriptor,4,RoundJson.serializer(),s.run { RoundJson(x,y,r) })
                else error("shape type logic error")
                style?.let { encodeSerializableElement(descriptor,5,ButtonStyle.serializer(),it) }
                stylePressed?.let { encodeSerializableElement(descriptor,6,ButtonStyle.serializer(),it) }
            }
        }
    }
}