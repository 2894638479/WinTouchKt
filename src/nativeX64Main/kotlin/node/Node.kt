package node

import dsl.State
import dsl.mutStateNull
import geometry.Color
import geometry.Font
import geometry.GREY_BRIGHT
import geometry.GREY_DARK
import geometry.Point
import geometry.RED
import geometry.WHITE
import wrapper.D2dBrush
import wrapper.D2dFont
import wrapper.SerializerWrapper
import kotlin.math.abs

abstract class Node : State.Scope {
    final override val _onDestroy: MutableList<() -> Unit> = mutableListOf()
    var parent by mutStateNull<NodeWithChild<*>>()
    val context: Container.Context? by combine { parent?.context }

    var name by mutStateNull<String>()
    abstract val defaultName: String
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
    val displayOutlineWidth:Float by combine {
        outlineWidth
            ?: parent?.displayOutlineWidth
            ?: 0f
    }


    val displayStyle = DisplayStyle(this,false)
    val displayPressedStyle = DisplayStyle(this,true)

    fun displayStyle(pressed:Boolean) = if(pressed) displayPressedStyle else displayStyle

    class DisplayStyle(private val node:Node,pressed:Boolean): State.Scope by node {
        companion object {
            private fun <T> Node.display(
                pressed: Boolean,
                default:(Boolean)->T,
                fromStyle:ButtonStyle.()->T?,
                fromDisplay:DisplayStyle.()->T?
            ) = combine {
                val thisStyle = style(pressed)
                val parentDisplay = parent?.displayStyle(pressed)
                    ?: return@combine thisStyle?.fromStyle() ?: default(pressed)
                thisStyle?.fromStyle() ?: parentDisplay.fromDisplay() ?: default(pressed)
            }
            fun defaultColor(pressed:Boolean) = if(pressed) GREY_BRIGHT else GREY_DARK
            fun defaultOutlineColor(pressed:Boolean) = WHITE
            fun defaultTextColor(pressed: Boolean) = RED
        }
        val color: Color by node.display(pressed,::defaultColor,{color}){color}
        val textColor: Color by node.display(pressed,::defaultTextColor,{textColor}){textColor}
        val outlineColor: Color by node.display(pressed,::defaultOutlineColor,{outlineColor}){outlineColor}
        val fontFamily: String by node.display(pressed,{""},{fontFamily}){fontFamily}
        val fontSize: Float by node.display(pressed,{24f},{fontSize}){fontSize}
        val fontStyle: Font.Style by node.display(pressed,{Font.Style.NORMAL},{fontStyle}){fontStyle}
        val fontWeight: Int by node.display(pressed,{500},{fontWeight}){fontWeight}

        val font : D2dFont? get() {
            val context = node.context ?: return null
            return context.drawScope.cache.font(
                Font(
                    fontFamily,
                    fontSize,
                    fontStyle,
                    fontWeight,
                    node.displayScale
                )
            )
        }
        val brush : D2dBrush? get() {
            val drawScope = node.context?.drawScope ?: return null
            return drawScope.cache.brush(color)
        }
        val textBrush : D2dBrush? get() {
            val drawScope = node.context?.drawScope ?: return null
            return drawScope.cache.brush(textColor)
        }
        val outlineBrush : D2dBrush? get() {
            if (node.displayOutlineWidth <= 0f) return null
            val drawScope = node.context?.drawScope ?: return null
            return drawScope.cache.brush(outlineColor)
        }
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
