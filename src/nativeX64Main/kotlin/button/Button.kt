package button

import buttonGroup.Group
import container.Node
import json.RectJson
import json.RoundedRectJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.CompositeDecoder
import logger.info
import wrapper.SerializerWrapper
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
        info("button $name down")
        keyHandler.downAll(key.filter(filter))
        counterIncrease()
        if(drawUi) toDraw(this@Button)
    }
    inline fun up(drawUi:Boolean = true,filter:(UByte)->Boolean = {true}) = scope.run {
        info("button $name up")
        keyHandler.upAll(key.filter(filter))
        counterDecrease()
        if(drawUi) toDraw(this@Button)
    }
    fun inArea(x:Float,y:Float) = shape.containPoint(x,y)

    object ButtonSerializer : SerializerWrapper<Button,ButtonSerializer.Descriptor>("Button",Descriptor){
        object Descriptor: SerializerWrapper.Descriptor<Button>() {
            val name = "name" from {name}
            val key = "key" from {key}
            val rect = "rect" from {(shape as? Rect)?.toRectJson()}
            val roundedRect = "roundedRect" from {(shape as? RoundedRect)?.toRoundedRectJson()}
            val round = "round" from {(shape as? Round)}
            val style = "style" from {style}
            val stylePressed = "stylePressed" from {stylePressed}
            override val items = listOf(name, key, rect, round, roundedRect, style, stylePressed)
        }
        override fun deserializeScope(decoder: CompositeDecoder) = object :DeserializeScope<Button, Descriptor>(decoder,this) {
            var name:String? = null
            var key:Set<UByte>? = null
            var rectJson:RectJson? = null
            var roundedRectJson:RoundedRectJson? = null
            var round:Round? = null
            var style:ButtonStyle? = null
            var stylePressed:ButtonStyle? = null
            init {
                desc.name to {name = it}
                desc.key to {key = it}
                desc.rect to {rectJson = it}
                desc.roundedRect to {roundedRectJson = it}
                desc.round to {round = it}
                desc.style to {style = it}
                desc.stylePressed to {stylePressed = it}
            }
            override fun end(): Button {
                var shapeCount = 0
                rectJson?.let { shapeCount++ }
                round?.let { shapeCount++ }
                roundedRectJson?.let { shapeCount++ }
                if(shapeCount != 1) error("choose one shape from rect round roundedRect")
                val shape = rectJson?.toRect()
                    ?: roundedRectJson?.toRoundedRect()
                    ?: round
                    ?: error("shape is null")
                return Button(key ?: error("button has no key"),shape).also {
                    it.style = style
                    it.stylePressed = stylePressed
                    it.name = name ?: error("button has no name")
                }
            }
        }
    }
}