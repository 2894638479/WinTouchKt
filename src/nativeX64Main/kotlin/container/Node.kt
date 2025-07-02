package container

import button.ButtonStyle
import button.Point
import button.Rect
import button.Shape
import draw.*
import wrapper.*

abstract class Node {
    var parent by WeakRefDel<NodeWithChild<*>>()
        internal set
    var scale:Float? = null
        set(value) {field = value.apply { cache.invalidateAll() }}
    var offset:Point? = null
        set(value) {field = value.apply { cache.invalidate() }}
    protected var style:ButtonStyle? = null
    protected var stylePressed:ButtonStyle? = null
    class StyleModifier(
        var style:ButtonStyle?,
        var stylePressed:ButtonStyle?
    )
    fun modifyStyle(block:StyleModifier.()->Unit){
        StyleModifier(style, stylePressed).let {
            it.block()
            style = it.style
            stylePressed = it.stylePressed
            cache.invalidateStyle()
        }
    }
    var name:String? = null
    abstract fun calOuterRect():Rect?
    open fun calCurrentShape():Shape? = null
    private inline fun iterateNodes(block:(Node)->Unit){
        var p:Node? = this
        while (p != null){
            block(p)
            p = p.parent
        }
    }
    val cache = Cache(this)
    class Cache(node:Node){
        private val node by WeakRefNonNull(node)
        val pressed = StyleCache(node){stylePressed}
        val unPressed = StyleCache(node){style}
        val scale:Float get() {
            var scale = 1f
            node.iterateNodes {
                it.scale?.let { scale *= it }
            }
            return scale
        }

        private var _outerRect:Rect? = null
        val outerRect get() = _outerRect ?: node.calOuterRect().also { _outerRect = it }

        private var _shape:Shape? = null
        val shape get() = _shape ?: node.calCurrentShape().also { _shape = it }

        private var _offset:Point? = null
        val offset:Point get() = _offset ?: run {
            var offset = Point(0f,0f)
            node.iterateNodes {
                it.scale?.let { offset *= it }
                it.offset?.let { offset += it }
            }
            offset.apply { _offset = this }
        }

        fun applyShape(orig:Shape):Shape{
            var final = orig
            node.iterateNodes {
                it.scale?.let { final = final.rescaled(it) }
                it.offset?.let { final = final.offset(it) }
            }
            return final
        }

        fun invalidate(){
            _outerRect = null
            _offset = null
        }
        fun invalidateStyle(){
            unPressed.invalidate()
            pressed.invalidate()
        }
        fun invalidateAll(){
            invalidate()
            invalidateStyle()
        }
        class StyleCache(node:Node,private val getStyle:Node.()->ButtonStyle?){
            private val node by WeakRefNonNull(node)
            private inline fun <T:Any> find(get:ButtonStyle.()->T?):T?{
                node.iterateNodes {
                    it.getStyle()?.get()?.let { return it }
                }
                return null
            }
            private inline fun <T:Any> find(default:T,get:ButtonStyle.()->T?) = find(get) ?: default
            private val color get() = find(BLACK){color}
            private val outlineColor get() = find(WHITE){outlineColor}
            val outlineWidth get() = find(1f){outlineWidth}
            private val textColor get() = find(RED){textColor}
            private val fontFamily get() = find{fontFamily}
            private val fontSize get() = find{fontSize}
            private val fontStyle get() = find{fontStyle}
            private val fontWeight get() = find{fontWeight}

            private var _font: D2dFont? = null
            val font get() = _font ?: Store.font(Font(fontFamily,fontSize,fontStyle,fontWeight,node.cache.scale))

            var _brush: D2dBrush? = null
            val brush get() = _brush ?: Store.brush(color).also { _brush = it }

            var _brushText: D2dBrush? = null
            val brushText get() = _brushText ?: Store.brush(textColor).also { _brushText = it }

            var _brushOutline: D2dBrush? = null
            val brushOutline get() = _brushOutline ?: Store.brush(outlineColor).also { _brushOutline = it }

            fun invalidate(){
                _font = null
                _brush = null
                _brushText = null
                _brushOutline = null
            }
        }
    }
}
