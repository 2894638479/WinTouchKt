package node

import dsl.mutStateOf
import error.wrapExceptionName
import geometry.Point
import geometry.minus
import geometry.plus
import geometry.Rect
import geometry.Round
import geometry.RoundedRect
import geometry.Shape
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger.info
import logger.warning
import wrapper.SerializerWrapper
import wrapper.d2dDrawText

@Serializable(with = Button.ButtonSerializer::class)
class Button(
    key:Set<UByte>,
    initShape: Shape,
): Node(){
    var key by mutStateOf(key)
    var shape by mutStateOf(initShape)
    var count by mutStateOf(0)
    val pressed by combine { count > 0 }

    val displayGeometry by combine { shape.rescaled(displayScale) to displayOffset }
    val onErase by combine<DrawScope.()->Unit> {
        val shape = displayGeometry.first
        val offset = displayGeometry.second
        val outlineWidth = displayOutlineWidth
        {
            with(offset){
                shape.d2dFill(target,cache.transparentBrush)
                if(outlineWidth > 0f) shape.d2dDraw(target,cache.transparentBrush,outlineWidth)
            }
        }
    }.apply {
        listen {
            context?.drawScope?.run {
                reDraw = true
            }
        }
    }
    val onDraw by combine<DrawScope.()->Unit> {
        wrapExceptionName("button onDraw failed") {
            context ?: return@combine {}
            val shape = displayGeometry.first
            val offset = displayGeometry.second
            val name = name
            val pressed = pressed
            val style = displayStyle(pressed)
            val brush = style.brush ?: return@combine {}
            val textBrush = style.textBrush ?: return@combine {}
            val outlineWidth = displayOutlineWidth
            val outlineBrush = style.outlineBrush
            val textBound = shape.padding(outlineWidth/2)?.outerRect
            val font = style.font ?: return@combine {}
            {
                with(offset) {
                    shape.d2dFill(target,brush)
                    if(outlineBrush != null) shape.d2dDraw(target,outlineBrush,outlineWidth)
                    if(textBound != null && name != null)
                        target.d2dDrawText(textBrush,font,textBound,name)
                }
            }
        }
    }.apply {
        listen {
            context?.drawScope?.addToDraw(it)
        }
    }

    inline fun down(filter:(UByte)->Boolean = {true}) = context?.run {
        info("button $name down")
        keyHandler.downAll(key.filter(filter))
        count++
    } ?: error("context is null")

    inline fun up(filter:(UByte)->Boolean = {true}) = context?.run {
        info("button $name up")
        if(count <= 0) return@run
        keyHandler.upAll(key.filter(filter))
        count--
    } ?: error("context is null")

    fun upAll() {
        while (count > 0) {
            up()
        }
    }

    fun triggerKeys() = context?.run {
        keyHandler.downAll(key)
        keyHandler.upAll(key)
    } ?: error("context is null")

    fun containPoint(x:Float, y:Float) = with(displayOffset){ Point(x,y) in shape.rescaled(displayScale) }


    fun snapTo(buttons:List<Button>){
        val outerRect = shape.outerRect
        var x: Float? = null
        var y: Float? = null
        buttons.forEach {
            if(it == this) return@forEach
            val otherRect = it.shape.outerRect
            val relaPos = (it.offset - offset) ?: return@forEach
            if(x == null)  x = outerRect.autoSnapToX(relaPos,otherRect,5f)
            if(y == null)  y = outerRect.autoSnapToY(relaPos,otherRect,5f)
        }
        offset += Point(x?:0f,y?:0f)
    }

    object ButtonSerializer : SerializerWrapper<Button, ButtonSerializer.Descriptor>("Button", Descriptor){
        object Descriptor: Node.Descriptor<Button>() {
            val key = "key" from {key}
            val rect = "rect" from {(shape as? Rect)}
            val roundedRect = "roundedRect" from {(shape as? RoundedRect)}
            val round = "round" from {(shape as? Round)}
        }
        override fun Descriptor.generate(): Button {
            var shapeCount = 0
            rect.nullable?.let { shapeCount++ }
            round.nullable?.let { shapeCount++ }
            roundedRect.nullable?.let { shapeCount++ }
            if(shapeCount != 1) error("choose one shape from rect round roundedRect")
            val shape = rect.nullable
                ?: roundedRect.nullable
                ?: round.nullable
                ?: error("shape is null")
            return Button(key.nonNull,shape).addNodeInfo()
        }
    }

    override fun toString() = "Button${Json.encodeToString(this)}"
}