package node

import dsl.mutStateOf
import geometry.Rect
import geometry.Round
import geometry.RoundedRect
import geometry.Shape
import kotlinx.serialization.Serializable
import logger.info
import logger.warning
import wrapper.SerializerWrapper

@Serializable(with = Button.ButtonSerializer::class)
class Button(
    var key:Set<UByte>,
    shape: Shape,
): Node(){
    private val shapeState = mutStateOf(shape) { _outerRect = null }
    var shape by shapeState
    private val displayShapeState = combine {
        shapeState.track.offset(displayOffset.track).rescaled(displayScale.track)
    }
    val displayShape by displayShapeState
    override fun calOuterRect() = displayShape.outerRect

    private val pressedState = mutStateOf(false)
    var pressed by pressedState
        private set

    private val countState = mutStateOf(0u) {
        pressed = it != 0u
    }
    var count by countState

    inline fun down(drawUi:Boolean = true,filter:(UByte)->Boolean = {true}) = context?.run {
        info("button $name down")
        keyHandler.downAll(key.filter(filter))
        count++
        if(drawUi) drawScope.toDraw(this@Button)
    } ?: error("context is null")
    inline fun up(drawUi:Boolean = true,filter:(UByte)->Boolean = {true}) = context?.run {
        info("button $name up")
        keyHandler.upAll(key.filter(filter))
        count--
        if(drawUi) drawScope.toDraw(this@Button)
    } ?: error("context is null")

    fun containPoint(x:Float, y:Float) = shape.containPoint(x,y)
    object ButtonSerializer : SerializerWrapper<Button, ButtonSerializer.Descriptor>("Button", Descriptor){
        object Descriptor: Node.Descriptor<Button>() {
            val key = "key" from {key}
            val rect = "rect" from {(shape as? Rect)}
            val roundedRect = "roundedRect" from {(shape as? RoundedRect)}
            val round = "round" from {(shape as? Round)}
        }
        override fun Descriptor.generate(): Button {
            warning("button1")
            var shapeCount = 0
            rect.nullable?.let { shapeCount++ }
            round.nullable?.let { shapeCount++ }
            roundedRect.nullable?.let { shapeCount++ }
            if(shapeCount != 1) error("choose one shape from rect round roundedRect")
            val shape = rect.nullable
                ?: roundedRect.nullable
                ?: round.nullable
                ?: error("shape is null")
            warning("button2")
            return Button(key.nonNull,shape).addNodeInfo()
        }
    }
}