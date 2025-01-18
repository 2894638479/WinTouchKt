package draw

import button.Button
import error.brushCreateError
import error.direct2dInitializeError
import error.fontCreateError
import kotlinx.cinterop.*
import libs.Clib.*
import platform.windows.*

@OptIn(ExperimentalForeignApi::class)
class DrawScope(
    private val hwnd:  CPointer<hwndHolder>?,
    val iterateButtons: ((Button) -> Unit) -> Unit
) {
    var alpha: UByte = 128u
    init{
        SetLayeredWindowAttributes(hwnd?.reinterpret(),0u,alpha, LWA_ALPHA.toUInt() or LWA_COLORKEY.toUInt())
    }
    var showStatus = true
        private set
    private var allInvalid = true
    private val factory : CPointerVar<d2dFactoryHolder> = nativeHeap
        .alloc<CPointerVar<d2dFactoryHolder>> {
            if(d2dCreateFactory(ptr) != 0) direct2dInitializeError()
        }
    private val target :CPointerVar<d2dTargetHolder> = nativeHeap
        .alloc<CPointerVar<d2dTargetHolder>> {
            if(d2dCreateTarget(factory.value,ptr,hwnd?.reinterpret()) != 0) direct2dInitializeError()
        }
    private val writeFactory:CPointerVar<d2dWriteFactoryHolder> = nativeHeap
        .alloc<CPointerVar<d2dWriteFactoryHolder>> {
            if(d2dCreateWriteFactory(ptr) != 0) direct2dInitializeError()
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
        ValidateRect(hwnd?.reinterpret(),null)
    }

    fun resize() {
        d2dResizeRenderTarget(target.value,hwnd)
        iterateButtons { invalidButtons += it }
    }

    private val invalidButtons = mutableSetOf<Button>().apply { iterateButtons{ add(it) } }
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
        allInvalid = true
    }

    private fun drawButton(button:Button){
        val scaleX:Float
        val scaleY:Float
        d2dGetDpi(target.value).useContents {
            scaleX = 96f/x
            scaleY = 96f/y
        }
        val l = button.rect.left.toFloat() * scaleX
        val t = button.rect.top.toFloat() * scaleY
        val r = button.rect.right.toFloat() * scaleX
        val b = button.rect.bottom.toFloat() * scaleY
        d2dDrawRect(
            target.value,
            brush(button.rectC),
            l,t,r,b
        )
//        d2dDrawText(
//            target.value,
//            brush(button.textC),
//            font()
//        )
    }
    private fun eraseButton(button:Button){
        val l = button.rect.left.toFloat()
        val t = button.rect.top.toFloat()
        val r = button.rect.right.toFloat()
        val b = button.rect.bottom.toFloat()
        d2dDrawRect(
            target.value,
            brush(BLACK),
            l,t,r,b
        )
    }

    private val brushes = mutableMapOf<Color,CValuesRef<d2dSolidColorBrushHolder>>()
    private val fonts = mutableMapOf<Font,CValuesRef<d2dTextFormatHolder>>()
    private fun font(key:Font) = fonts[key] ?: memScoped {
        val font = nativeHeap.alloc<CPointerVar<d2dTextFormatHolder>>()
        d2dCreateTextFormat(
            writeFactory.value,
            font.ptr,
            key.family?.wcstr,
            key.size,
            key.weight,
            key.style,
            FONT_STRETCH_MEDIUM
        )
        val fontPtr = font.value ?: fontCreateError()
        fonts[key] = fontPtr
        fontPtr
    }
    private fun brush(key:Color) = brushes[key] ?: memScoped {
        val brush = nativeHeap.alloc<CPointerVar<d2dSolidColorBrushHolder>>()
        d2dCreateSolidColorBrush(
            target.value,
            brush.ptr,
            key.r.toFloat() / 255f,
            key.g.toFloat() / 255f,
            key.b.toFloat() / 255f,
            1f
        )
        val brushPtr = brush.value ?: brushCreateError()
        brushes[key] = brushPtr
        brushPtr
    }
    fun destruct() {
        d2dFreeFactory(factory.value)
        d2dFreeTarget(target.value)
        d2dFreeWriteFactory(writeFactory.value)
        for((_,it) in brushes) { d2dFreeSolidColorBrush(it) }
        for((_,it) in fonts) { d2dFreeTextFormat(it) }
    }
}