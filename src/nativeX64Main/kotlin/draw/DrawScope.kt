package draw

import button.Button
import error.direct2dInitializeError
import kotlinx.cinterop.*
import libs.Clib.*
import platform.windows.InvalidateRect
import platform.windows.LWA_ALPHA
import platform.windows.LWA_COLORKEY
import platform.windows.SetLayeredWindowAttributes
import wrapper.D2dFactory
import wrapper.D2dTarget
import wrapper.D2dWriteFactory
import wrapper.Hwnd

@OptIn(ExperimentalForeignApi::class)
class DrawScope(
    private val hwnd: Hwnd,
) {
    var iterateButtons: ((Button) -> Unit) -> Unit = {}
        set(value)  {
            field = value
            value { invalidButtons += it }
        }
    var alpha: UByte = 128u
        set(value) {
            field = value
            SetLayeredWindowAttributes(hwnd.HWND,0u,value, LWA_ALPHA.toUInt() or LWA_COLORKEY.toUInt())
        }
    init{ alpha = 128u }
    var showStatus = true
        private set
    private val factory = D2dFactory.create()
    private val target = D2dTarget.create(hwnd,factory)
    private val writeFactory = D2dWriteFactory.create()
    fun initStore(){
        Store.target = target
        Store.writeFactory = writeFactory
    }
    private inline fun d2dDraw(block:()->Unit){
        d2dBeginDraw(target.value)
        block()
        d2dEndDraw(target.value)
    }
    private var clear = false
    fun onDraw() {
        if(invalidButtons.isNotEmpty() || clear) {
            d2dDraw {
                if(clear) {
                    clear = false
                    d2dClear(target.value)
                }
                invalidButtons.forEach {
                    drawButton(it)
                }
                invalidButtons.clear()
            }
        }
    }

    fun resize() {
        d2dResizeRenderTarget(target.value,hwnd.value)
        iterateButtons { invalidate(it) }
    }

    private val invalidButtons = mutableSetOf<Button>()
    fun invalidate(button: Button) {
        invalidButtons += button
        InvalidateRect(hwnd.HWND,null,1)
    }

    fun hideButtons(controller: Button) {
        showStatus = false
        clear = true
        invalidate(controller)
    }
    fun showButtons() {
        showStatus = true
        iterateButtons {invalidate(it)}
    }

    private fun drawButton(button:Button){
        val target = target.value
        button.apply {
            val style = findCorrectStyle()
            calCurrentShape.d2dFill(target, style)
            if(name.isNotEmpty())
                calCurrentShape.d2dDrawText(target,style,name)
            calCurrentShape.d2dDraw(target, style)
        }
    }

    fun destruct() {
        factory.free()
        target.free()
        writeFactory.free()
        iterateButtons = {}
    }
}