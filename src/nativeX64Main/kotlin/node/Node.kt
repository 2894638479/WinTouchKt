package node

import dsl.MutState
import dsl.mutStateNull
import geometry.*
import wrapper.*

abstract class Node : MutState.Scope {
    final override val _onDestroy: MutableList<() -> Unit> = mutableListOf()
    val parentState = mutStateNull<NodeWithChild<*>>()
    var parent by parentState

    val contextState:MutState<Container.Context?> = combine { parentState.track?.contextState?.track }
    var context by contextState

    val nameState = mutStateNull<String>()
    val scaleState = mutStateNull<Float>()
    val offsetState = mutStateNull<Point>()
    val styleState = mutStateNull<ButtonStyle>()
    val stylePressedState = mutStateNull<ButtonStyle>()
    val outlineWidthState = mutStateNull<Float>()

    fun styleState(pressed:Boolean) = if(pressed) stylePressedState else styleState

    var name by nameState
    var scale by scaleState
    var offset by offsetState
    var style by styleState
    var stylePressed by stylePressedState
    var outlineWidth by outlineWidthState

    abstract fun calOuterRect():Rect
    protected var _outerRect : Rect? = null
        set(value) {
            if(field != value) {
                field = value
                if (field == null) {
                    parent?.apply { _outerRect = null }
                }
            }
        }
    val outerRect get() = _outerRect ?: calOuterRect().apply { _outerRect = this }

    val displayScale:MutState<Float> = combine {
        val scale = scaleState.track ?: 1f
        parentState.track?.displayScale?.track?.let {
            it * scale
        } ?: scale
    }
    val displayOffset:MutState<Point> = combine {
        val parentScale = parentState.track?.displayScale?.track ?: 1f
        val parentOffset = parentState.track?.displayOffset?.track ?: Point.origin
        val offset = offsetState.track ?: Point.origin
        parentOffset + (offset * parentScale)
    }

    init {
        displayScale.listen { _outerRect = null }
        displayOffset.listen { _outerRect = null }
    }


    val displayStyle = DisplayStyle(this,false)
    val displayPressedStyle = DisplayStyle(this,true)

    fun displayStyle(pressed:Boolean) = if(pressed) displayPressedStyle else displayStyle

    class DisplayStyle(node:Node,pressed:Boolean){
        companion object {
            private fun <T> Node.display(pressed: Boolean,
                fromStyle:ButtonStyle.()->MutState<T>, fromDisplay:DisplayStyle.()->MutState<T>) = combine {
                val thisStyle = styleState(pressed).track
                val parentDisplay = parentState.track?.displayStyle(pressed) ?: return@combine thisStyle?.fromStyle()?.track
                parentDisplay.fromDisplay().track ?: thisStyle?.fromStyle()?.track
            }
        }
        val color:MutState<Color?> = node.display(pressed,{colorState}){color}
        val textColor:MutState<Color?> = node.display(pressed,{textColorState}){textColor}
        val outlineColor:MutState<Color?> = node.display(pressed,{outlineColorState}){outlineColor}
        val fontFamily:MutState<String?> = node.display(pressed,{fontFamilyState}){fontFamily}
        val fontSize:MutState<Float?> = node.display(pressed,{fontSizeState}){fontSize}
        val fontStyle:MutState<Font.Style?> = node.display(pressed,{fontStyleState}){fontStyle}
        val fontWeight:MutState<Int?> = node.display(pressed,{fontWeightState}){fontWeight}

        val fontState = node.combine {
            val context = node.contextState.track ?: return@combine null
            context.drawScope.cache.font(Font(
                fontFamily.track,
                fontSize.track,
                fontStyle.track,
                fontWeight.track,
                node.displayScale.track
            ))
        }
        val brushState = node.combine {
            val drawScope = node.contextState.track?.drawScope ?: return@combine null
            drawScope.cache.brush(color.track ?: drawScope.defaultColor(pressed))
        }
        val textBrushState = node.combine {
            val drawScope = node.contextState.track?.drawScope ?: return@combine null
            drawScope.cache.brush(textColor.track ?: drawScope.defaultTextColor(pressed))
        }
        val outlineBrushState = node.combine {
            if(node.outlineWidthState.track.let { (it ?: 0f) <= 0f }) return@combine null
            val drawScope = node.contextState.track?.drawScope ?: return@combine null
            drawScope.cache.brush(outlineColor.track ?: drawScope.defaultOutlineColor(pressed))
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
