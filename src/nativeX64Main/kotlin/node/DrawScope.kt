package node

import dsl.mutStateOf
import geometry.*
import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.d2dBeginDraw
import libs.Clib.d2dClear
import libs.Clib.d2dCreateTarget
import libs.Clib.d2dDrawRect
import libs.Clib.d2dEndDraw
import libs.Clib.d2dFillRect
import libs.Clib.d2dResizeRenderTarget
import logger.info
import logger.warning
import platform.windows.LWA_ALPHA
import platform.windows.LWA_COLORKEY
import platform.windows.SetLayeredWindowAttributes
import sendInput.Keys
import wrapper.*

@OptIn(ExperimentalForeignApi::class)
class DrawScope(private val buttons:Sequence<Button>,val cursorPos:()->Point?, val hwnd: Hwnd) {
    var alpha: UByte = 128u
        set(value) {
            field = value
            SetLayeredWindowAttributes(hwnd.HWND,0u,value, LWA_ALPHA.toUInt() or LWA_COLORKEY.toUInt())
        }
    val factory = D2dFactory.create()
    val target = D2dTarget.create(hwnd,factory)
    val writeFactory = D2dWriteFactory.create()

    fun drawCursor(){
        val pos = cursorPos() ?: return
        val brush = cache.brush(RED)
        val halfWidth = 1f
        target.d2dFillRect(brush,pos.x - halfWidth,-10000f,pos.x + halfWidth,10000f)
        target.d2dFillRect(brush,-10000f,pos.y - halfWidth,10000f,pos.y + halfWidth)
    }

    private inline fun d2dDraw(block:()->Unit){
        d2dBeginDraw(target.value)
        block()
        d2dEndDraw(target.value)
    }
    var reDraw = true.apply { hwnd.invalidateRect() }
        set(value) {
            field = value
            if(value) hwnd.invalidateRect()
        }
    fun onDraw() = d2dDraw {
        if(reDraw) {
            d2dClear(target.value)
            buttons.forEach { it.onDraw(this) }
            drawCursor()
        } else {
            toErase.forEach{ it() }
            toDraw.forEach { it() }
        }
        reDraw = false
        toErase.clear()
        toDraw.clear()
        hwnd.validateRect()
    }


    fun resize() {
        d2dResizeRenderTarget(target.value,hwnd.value)
        reDraw = true
    }
    private val toDraw = mutableListOf<DrawScope.()->Unit>()
    private val toErase = mutableListOf<DrawScope.()->Unit>()
    fun addToDraw(block:DrawScope.()->Unit){
        toDraw += block
        hwnd.invalidateRect()
    }
    fun addToErase(block:DrawScope.()->Unit){
        toErase += block
        hwnd.invalidateRect()
    }

    var showStatus = true
        set(value) {
            field = value
            if(value) {
                reDraw = true
            } else {
                buttons.filter { !it.key.contains(Keys.HIDE_SHOW.code) }.forEach{addToErase(it.onErase)}
            }
        }

    fun destroy() {
        cache.destroy()
        factory.free()
        target.free()
        writeFactory.free()
    }
    private var cacheNotifier by mutStateOf(false)
    private val _cache = Cache(writeFactory,target){
        cacheNotifier = !cacheNotifier
        toDraw.clear()
        toErase.clear()
        reDraw = true
    }
    val cache get() = _cache.apply { cacheNotifier }
    class Cache(private val writeFactory: D2dWriteFactory, private val target: D2dTarget,val notifyCleared:()->Unit) {
        private val brushes = HashMap<Color, D2dBrush>(100)
        private val fonts = HashMap<Font, D2dFont>(100)
        private fun clearBrushes() {
            brushes.forEach { (_,brush) -> brush.free() }
            brushes.clear()
            info("brushes cache cleared")
            notifyCleared()
        }
        private fun clearFonts() {
            fonts.forEach { (_,font) -> font.free() }
            fonts.clear()
            info("fonts cache cleared")
            notifyCleared()
        }
        fun checkOrClear() {
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