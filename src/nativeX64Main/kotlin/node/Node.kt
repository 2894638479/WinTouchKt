package node

import dsl.MutState
import dsl.mutStateNull
import geometry.*
import geometry.Point
import wrapper.*
import kotlin.math.abs

abstract class Node : MutState.Scope {
    final override val _onDestroy: MutableList<() -> Unit> = mutableListOf()
    var parent by mutStateNull<NodeWithChild<*>>()
    var context: Container.Context? by combine { parent?.context }

    var name by mutStateNull<String>()
    var scale by mutStateNull<Float>(constraint = { it?.let { if (it == 0f) 0.1f else abs(it) } })
    var offset by mutStateNull<Point>()
    var style by mutStateNull<ButtonStyle>()
    var stylePressed by mutStateNull<ButtonStyle>()
    var outlineWidth by mutStateNull<Float>()

    fun style(pressed:Boolean) = if(pressed) stylePressed else style

    val displayScale:Float by combine {
        val scale = scale ?: 1f
        parent?.displayScale?.let {
            it * scale
        } ?: scale
    }
    val displayOffset:Point by combine {
        val parentScale = parent?.displayScale ?: 1f
        val parentOffset = parent?.displayOffset ?: Point.origin
        val offset = offset ?: Point.origin
        parentOffset + (offset * parentScale)
    }


    val displayStyle = DisplayStyle(this,false)
    val displayPressedStyle = DisplayStyle(this,true)

    fun displayStyle(pressed:Boolean) = if(pressed) displayPressedStyle else displayStyle

    class DisplayStyle(node:Node,pressed:Boolean){
        companion object {
            private fun <T> Node.display(pressed: Boolean,
                fromStyle:ButtonStyle.()->T, fromDisplay:DisplayStyle.()->T) = combine {
                val thisStyle = style(pressed)
                val parentDisplay = parent?.displayStyle(pressed) ?: return@combine thisStyle?.fromStyle()
                thisStyle?.fromStyle() ?: parentDisplay.fromDisplay()
            }
        }
        val color: Color? by node.display(pressed,{color}){color}
        val textColor: Color? by node.display(pressed,{textColor}){textColor}
        val outlineColor: Color? by node.display(pressed,{outlineColor}){outlineColor}
        val fontFamily: String? by node.display(pressed,{fontFamily}){fontFamily}
        val fontSize: Float? by node.display(pressed,{fontSize}){fontSize}
        val fontStyle: Font.Style? by node.display(pressed,{fontStyle}){fontStyle}
        val fontWeight: Int? by node.display(pressed,{fontWeight}){fontWeight}

        val fontState = node.combine {
            val context = node.context ?: return@combine null
            context.drawScope.cache.font(Font(
                fontFamily,
                fontSize,
                fontStyle,
                fontWeight,
                node.displayScale
            ))
        }
        val brushState = node.combine {
            val drawScope = node.context?.drawScope ?: return@combine null
            drawScope.cache.brush(color ?: drawScope.defaultColor(pressed))
        }
        val textBrushState = node.combine {
            val drawScope = node.context?.drawScope ?: return@combine null
            drawScope.cache.brush(textColor ?: drawScope.defaultTextColor(pressed))
        }
        val outlineBrushState = node.combine {
            if(node.outlineWidth.let { (it ?: 0f) <= 0f }) return@combine null
            val drawScope = node.context?.drawScope ?: return@combine null
            drawScope.cache.brush(outlineColor ?: drawScope.defaultOutlineColor(pressed))
        }
        val font by fontState
        val brush by brushState
        val textBrush by textBrushState
        val outlineBrush by outlineBrushState
    }

    open class Descriptor<T:Node> : SerializerWrapper.Descriptor<T>() {
        val name = "name" from {name}
        val offset = "offset" from {offset}
        val scale = "scale" from {scale}
        val outlineWidth = "outlineWidth" from {outlineWidth}
        val style = "style" from {style}
        val stylePressed = "stylePressed" from {stylePressed}
        fun T.addNodeInfo() = apply {
            val desc = this@Descriptor
            name = desc.name.nullable
            offset = desc.offset.nullable
            scale = desc.scale.nullable
            style = desc.style.nullable
            stylePressed = desc.stylePressed.nullable
            outlineWidth = desc.outlineWidth.nullable
        }
    }
}
