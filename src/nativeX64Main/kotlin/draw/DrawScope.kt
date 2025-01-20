package draw

import button.Button
import error.direct2dInitializeError
import kotlinx.cinterop.*
import libs.Clib.*
import platform.windows.InvalidateRect
import platform.windows.LWA_ALPHA
import platform.windows.LWA_COLORKEY
import platform.windows.SetLayeredWindowAttributes

@OptIn(ExperimentalForeignApi::class)
class DrawScope(
    private val hwnd:  CPointer<hwndHolder>?,
) {
    var iterateButtons: ((Button) -> Unit) -> Unit = {}
        set(value)  {
            field = value
            value { invalidButtons += it }
        }
    var alpha: UByte = 128u
        set(value) {
            field = value
            SetLayeredWindowAttributes(hwnd?.reinterpret(),0u,value, LWA_ALPHA.toUInt() or LWA_COLORKEY.toUInt())
        }
    init{ alpha = 128u }
    var showStatus = true
        private set
    private val factory : CPointerVar<d2dFactoryHolder> = nativeHeap
        .alloc<CPointerVar<d2dFactoryHolder>> {
            if(d2dCreateFactory(ptr) != 0) direct2dInitializeError()
        }
    private val target :CPointerVar<d2dTargetHolder> = nativeHeap
        .alloc<CPointerVar<d2dTargetHolder>> {
            if(d2dCreateTarget(factory.value,ptr,hwnd?.reinterpret()) != 0) direct2dInitializeError()
            //关闭抗锯齿，避免边缘带线
            d2dSetAntialiasMode(value,false)
        }
    private val writeFactory:CPointerVar<d2dWriteFactoryHolder> = nativeHeap
        .alloc<CPointerVar<d2dWriteFactoryHolder>> {
            if(d2dCreateWriteFactory(ptr) != 0) direct2dInitializeError()
        }
    fun initStore(){
        Store.target = target.value
        Store.writeFactory = writeFactory.value
    }
    private inline fun d2dDraw(block:()->Unit){
        d2dBeginDraw(target.value)
        block()
        d2dEndDraw(target.value)
    }
    fun onDraw() {
        if(invalidButtons.isNotEmpty()) {
            d2dDraw {
                invalidButtons.forEach {
                    drawButton(it)
                }
                invalidButtons.clear()
            }
        }
    }

    fun resize() {
        d2dResizeRenderTarget(target.value,hwnd)
        iterateButtons { invalidButtons += it }
    }

    private val invalidButtons = mutableSetOf<Button>()
    fun invalidate(button: Button) {
        invalidButtons += button
        InvalidateRect(hwnd?.reinterpret(),null,1)
    }

    fun hideButtons(controller: Button) {
        iterateButtons {
            if(it != controller) {
                invalidate(it)
            }
        }
    }
    fun showButtons() {
        showStatus = true
    }

    private fun drawButton(button:Button){
        button.apply {
            val target = target.value
            val style = currentStyle
            shape.d2dFill(target, style)
            if(name.isNotEmpty())
                shape.d2dDrawText(target,style,name)
            shape.d2dDraw(target, style)
        }
    }

    fun destruct() {
        d2dFreeFactory(factory.value)
        d2dFreeTarget(target.value)
        d2dFreeWriteFactory(writeFactory.value)
        iterateButtons = {}
    }
}