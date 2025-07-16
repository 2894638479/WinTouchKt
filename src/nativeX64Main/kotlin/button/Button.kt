package button

import buttonGroup.Group
import container.Node
import kotlinx.serialization.Serializable
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
        }
        override fun Descriptor.generate(): Button {
            var shapeCount = 0
            rect.nullable?.let { shapeCount++ }
            round.nullable?.let { shapeCount++ }
            roundedRect.nullable?.let { shapeCount++ }
            if(shapeCount != 1) error("choose one shape from rect round roundedRect")
            val shape = rect.nullable?.toRect()
                ?: roundedRect.nullable?.toRoundedRect()
                ?: round.nullable
                ?: error("shape is null")
            return Button(key.nullable ?: error("button has no key"),shape).also {
                it.style = style.nullable
                it.stylePressed = stylePressed.nullable
                it.name = name.nullable
            }
        }
    }
}