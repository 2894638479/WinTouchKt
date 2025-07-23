package node

import geometry.*
import kotlinx.cinterop.*
import libs.Clib.*
import logger.warning
import platform.windows.*
import sendInput.KeyHandler.Companion.KEY_HIDE
import wrapper.*

@OptIn(ExperimentalForeignApi::class)
class DrawScope(private val buttons:Sequence<Button>, val hwnd: Hwnd) {
    var alpha: UByte = 128u
        set(value) {
            field = value
            SetLayeredWindowAttributes(hwnd.HWND,0u,value, LWA_ALPHA.toUInt() or LWA_COLORKEY.toUInt())
        }
    private val factory = D2dFactory.create()
    private val target = D2dTarget.create(hwnd,factory)
    private val writeFactory = D2dWriteFactory.create()
    private inline fun d2dDraw(block:()->Unit){
        d2dBeginDraw(target.value)
        block()
        d2dEndDraw(target.value)
    }
    private var reDraw = true.apply { hwnd.invalidateRect() }
    fun onDraw() = d2dDraw {
        warning("onDraw invoked")
        toErase.forEach { (shape,w) ->
            shape.d2dFill(target,cache.transparentBrush)
            if(w != null) shape.d2dDraw(target,cache.transparentBrush,w)
        }
        toErase.clear()
        fun drawButton(button: Button){
            warning("drawing $button at ${button.displayShape} alpha $alpha")
            val style = button.displayStyle(button.pressed)
            val shape = button.displayShape
            val outlineWidth = button.outlineWidth
            shape.d2dFill(target,style.brush ?: error("brush is null"))
            if(outlineWidth != null) shape.d2dDraw(target,style.outlineBrush ?: error("outlineBrush is null"),outlineWidth)
            val textBound = if(outlineWidth == null) shape.innerRect else shape.padding(outlineWidth)?.innerRect ?: return
            val text = button.name ?: return
            target.d2dDrawText(style.textBrush ?: error("textBrush is null"),
                style.font ?: error("font is null"),textBound,text)
        }
        if(reDraw) buttons.forEach(::drawButton)
        else toDraw.forEach(::drawButton)
        reDraw = false
        toDraw.clear()
        hwnd.validateRect()
    }


    fun resize() {
        d2dResizeRenderTarget(target.value,hwnd.value)
        toDrawAll()
    }

    private val toDraw = mutableSetOf<Button>()
    private val toErase = mutableListOf<Pair<Shape,Float?>>()
    fun toDraw(button: Button) {
        warning(toDraw.toString()); toDraw += button; hwnd.invalidateRect() }
    fun toErase(button: Button) { toErase += button.shape to button.outlineWidth; hwnd.invalidateRect() }
    fun toDrawAll() { reDraw = true; hwnd.invalidateRect() }
    var showStatus = true
        set(value) {
            field = value
            if(value) {
                reDraw = true
            } else {
                buttons.filter { !it.key.contains(KEY_HIDE) }.forEach(::toErase)
            }
        }

    fun destroy() {
        cache.destroy()
        factory.free()
        target.free()
        writeFactory.free()
    }
    fun defaultColor(pressed:Boolean) = if(pressed) GREY_BRIGHT else GREY_DARK
    fun defaultOutlineColor(pressed:Boolean) = WHITE
    fun defaultTextColor(pressed: Boolean) = RED
    val cache = Cache(writeFactory,target)
    class Cache(private val writeFactory: D2dWriteFactory, private val target: D2dTarget) {
        private val brushes = HashMap<Color, D2dBrush>(100)
        private val fonts = HashMap<Font, D2dFont>(100)
        fun clearBrushes() = brushes.forEach { (_,brush) -> brush.free() }.run { brushes.clear() }
        fun clearFonts() = fonts.forEach { (_,font) -> font.free() }.run { fonts.clear() }
        private fun checkOrClear() {
            if (brushes.count() >= 200) clearBrushes()
            if (fonts.count() >= 200) clearFonts()
        }
        fun destroy(){
            clearBrushes()
            clearFonts()
            transparentBrush.free()
        }

        fun font(key: Font) = fonts[key] ?: checkOrClear()
            .run { writeFactory }
            .let { D2dFont.create(it,key) }
            .apply { fonts[key] = this }

        fun brush(key: Color) = brushes[key] ?: checkOrClear()
            .run { target }
            .let { D2dBrush.create(it,key) }
            .apply { brushes[key] = this }

        val transparentBrush = D2dBrush.create(target, BLACK)
    }
}