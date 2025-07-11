package button

import buttonGroup.Group
import container.Node
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
import wrapper.WeakRefDel

@Serializable(with = Button.ButtonSerializer::class)
class Button(
    var key:Set<UByte>,
    shapeOrig:Shape,
):Node(){
    var shapeOrig = shapeOrig
        set(value) { field = value.apply { cache.invalidate() } }
    val shape get() = cache.shape ?: error("button get shape error")
    override var parent by WeakRefDel<Group>()
    override fun calOuterRect() = shape.outerRect
    override fun calCurrentShape() = cache.applyShape(shapeOrig)
    inline val currentStyle get() = if(pressed) cache.pressed else cache.unPressed
    var count = 0u
        private set
    fun counterIncrease() = count++
    fun counterDecrease() = count--
    inline val pressed get() = count != 0u
    inline val container get() = parent?.parent ?: error("button find container failed")
    inline val scope get() = container.touchScope

    inline fun down(drawUi:Boolean = true,filter:(UByte)->Boolean = {true}) = scope.run {
        keyHandler.downAll(key.filter(filter))
        counterIncrease()
        if(drawUi) toDraw(this@Button)
    }
    inline fun up(drawUi:Boolean = true,filter:(UByte)->Boolean = {true}) = scope.run {
        keyHandler.upAll(key.filter(filter))
        counterDecrease()
        if(drawUi) toDraw(this@Button)
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
            if(shapeCount != 1) error("choose one shape from rect round roundedRect")
            val shape = rectJson?.toRect()
                ?: roundedRectJson?.toRoundedRect()
                ?: roundJson?.toRound()
                ?: error("shape is null")
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