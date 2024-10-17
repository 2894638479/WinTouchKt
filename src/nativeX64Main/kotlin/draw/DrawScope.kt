package draw

import button.*
import kotlinx.cinterop.*
import platform.windows.*


@OptIn(ExperimentalForeignApi::class)
class DrawScope(private val hWnd: HWND?) {

    private val drawArea = nativeHeap.alloc<RECT> { GetClientRect(hWnd, this.ptr) }
    private val size = nativeHeap.alloc<SIZE> {cx = drawArea.width; cy = drawArea.height}
    private val pt = nativeHeap.alloc<POINT> {x = drawArea.left; y = drawArea.top}
    private fun updateDrawArea() = memScoped {
        val newArea = alloc<RECT> { GetClientRect(hWnd, this.ptr) }
        if(!drawArea.equal(newArea)){
            drawArea.copyFrom(newArea)
            onAreaChange()
        }
    }
    private fun onAreaChange(){
        size.cx = drawArea.width
        size.cy = drawArea.height
        pt.x = drawArea.left
        pt.y = drawArea.top
        bitmap = null
        buttons { invalidate(it) }
    }
    var alpha get() = blendFunction.SourceConstantAlpha
        set(value) { blendFunction.SourceConstantAlpha = value }
    private val blendFunction = nativeHeap.alloc<BLENDFUNCTION> {
        SourceConstantAlpha = 255u
        AlphaFormat = 0u
        BlendOp = AC_SRC_OVER.toUByte()
        BlendFlags = 0u
    }

    private var bitmap:HBITMAP? = null
    private inline fun withHdcMem(block:(HDC?)->Unit){
        val ps = nativeHeap.alloc<PAINTSTRUCT>()
        val hdc = BeginPaint(hWnd,ps.ptr)
        if (bitmap == null) bitmap = CreateCompatibleBitmap(hdc,size.cx,size.cy)
        val hdcMem = CreateCompatibleDC(hdc).apply {
            SetBkMode(this, TRANSPARENT)
        }
        SelectObject(hdcMem, bitmap)
        block(hdcMem)
        DeleteObject(hdcMem)
        EndPaint(hWnd,ps.ptr)
    }

    private val invalidButtons = mutableSetOf<Button>()
    fun invalidate(button:Button) {
        invalidButtons += button
        InvalidateRect(hWnd,null, FALSE)
    }

    fun updateWindow() {
        updateDrawArea()
        if(invalidButtons.isNotEmpty()){
            withHdcMem{ hdcMem ->
                drawButtons(hdcMem, invalidButtons)
                invalidButtons.clear()
                updateLayeredWindow(hdcMem)
            }
        }
    }

    var showStatus = true
        private set
    fun hideButtons(controller: Button) = withHdcMem { hdcMem ->
        SelectObject(hdcMem,BLACK.brush)
        Rectangle(hdcMem,drawArea.left,drawArea.top,drawArea.right,drawArea.bottom)
        drawButton(hdcMem,controller)
        updateLayeredWindow(hdcMem)
        showStatus = false
    }

    fun showButtons(){
        buttons { invalidate(it) }
        updateWindow()
        showStatus = true
    }
    private var buttons:((Button)->Unit)->Unit = {  }
    fun addButtons(newButtons:((Button)->Unit)->Unit){
        val oldButtons = buttons
        buttons = {
            oldButtons(it)
            newButtons(it)
        }
        newButtons { invalidate(it) }
    }
    private fun updateLayeredWindow(hdc:HDC?) {
        UpdateLayeredWindow(hWnd, null, null, size.ptr, hdc, pt.ptr, 0u, blendFunction.ptr, (ULW_COLORKEY or ULW_ALPHA).toUInt())
    }
    fun destruct(){
        nativeHeap.free(drawArea)
        nativeHeap.free(size)
        nativeHeap.free(pt)
        nativeHeap.free(blendFunction)
    }
}