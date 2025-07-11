package container

import button.ButtonStyle
import button.Point
import button.Rect
import button.Shape
import draw.*
import wrapper.*

abstract class Node {
    abstract val parent:NodeWithChild<*>?
    var scale:Float? = null
        set(value) {field = value.apply { iterateChildren{ it.cache.invalidateAll() } }}
    var offset:Point? = null
        set(value) {field = value.apply { iterateChildren{ it.cache.invalidate() } }}
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
            iterateChildren{ it.cache.invalidateStyle() }
        }
    }
    var name:String? = null
    abstract fun calOuterRect():Rect?
    open fun calCurrentShape():Shape? = null
    protected open fun findDrawScopeCache():DrawScope.Cache = parent?.findDrawScopeCache() ?: error("find drawscope cache failed")
    private inline fun iterateParents(block:(Node)->Unit){
        var p:Node? = this
        while (p != null){
            block(p)
            p = p.parent
        }
    }
    open fun iterateChildren(block:(Node)->Unit) = block(this)
    val cache = Cache(this)
    class Cache(node:Node){
        private val node by WeakRefNonNull(node)
        val pressed = StyleCache(node, GREY_BRIGHT){stylePressed}
        val unPressed = StyleCache(node, GREY_DARK){style}
        val scale:Float get() {
            var scale = 1f
            node.iterateParents {
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
            node.iterateParents {
                it.scale?.let { offset *= it }
                it.offset?.let { offset += it }
            }
            offset.apply { _offset = this }
        }

        fun applyShape(orig:Shape):Shape{
            var final = orig
            node.iterateParents {
                it.scale?.let { final = final.rescaled(it) }
                it.offset?.let { final = final.offset(it) }
            }
            return final
        }

        fun invalidate(){
            _outerRect = null
            _offset = null
            _shape = null
        }
        fun invalidateStyle(){
            unPressed.invalidate()
            pressed.invalidate()
        }
        fun invalidateAll(){
            invalidate()
            invalidateStyle()
        }
        class StyleCache(node:Node,private val defaultColor: Color,private val getStyle:Node.()->ButtonStyle?){
            private val node by WeakRefNonNull(node)
            private val outerCache get() = node.findDrawScopeCache()
            private inline fun <T:Any> find(get:ButtonStyle.()->T?):T?{
                node.iterateParents {
                    it.getStyle()?.get()?.let { return it }
                }
                return null
            }
            private inline fun <T:Any> find(default:T,get:ButtonStyle.()->T?) = find(get) ?: default
            private val color get() = find(defaultColor){color}
            private val outlineColor get() = find(WHITE){outlineColor}
            val outlineWidth get() = find(1f){outlineWidth}
            private val textColor get() = find(RED){textColor}
            private val fontFamily get() = find{fontFamily}
            private val fontSize get() = find{fontSize}
            private val fontStyle get() = find{fontStyle}
            private val fontWeight get() = find{fontWeight}

            private var _font: D2dFont? = null
            val font get() = _font ?: outerCache.font(Font(fontFamily,fontSize,fontStyle,fontWeight,node.cache.scale))

            private var _brush: D2dBrush? = null
            val brush get() = _brush ?: outerCache.brush(color).also { _brush = it }

            private var _brushText: D2dBrush? = null
            val brushText get() = _brushText ?: outerCache.brush(textColor).also { _brushText = it }

            private var _brushOutline: D2dBrush? = null
            val brushOutline get() = _brushOutline ?: outerCache.brush(outlineColor).also { _brushOutline = it }

            fun invalidate(){
                _font = null
                _brush = null
                _brushText = null
                _brushOutline = null
            }
        }
    }
}
