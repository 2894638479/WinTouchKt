package draw

import button.*
import kotlinx.cinterop.*
import mainContainer
import platform.windows.*


@OptIn(ExperimentalForeignApi::class)
class DrawScope(val hWnd: HWND?) {
    val colorBrushBright = CreateSolidBrush(rgb(200,200,200))
    val colorBrushDark = CreateSolidBrush(rgb(50,50,50))
    val textBrush = GetStockObject(DEFAULT_GUI_FONT)

    val drawArea = nativeHeap.alloc<RECT>().apply { GetClientRect(hWnd, this.ptr) }
    private val size = nativeHeap.alloc<SIZE>().apply { cx = drawArea.width; cy = drawArea.height }
    private val pt = nativeHeap.alloc<POINT>().apply { x = 0;y = 0 }
    private val blendFunction = nativeHeap.alloc<BLENDFUNCTION>().apply {
        SourceConstantAlpha = 128u
        AlphaFormat = AC_SRC_OVER.toUByte()
        BlendOp = 0u
        BlendFlags = 0u
    }

    private val invalidButtons = mutableSetOf<Button>()
    fun invalidate(button:Button) {
        invalidButtons += button
        InvalidateRect(hWnd,null,TRUE)
    }

    private var bitmap:HBITMAP? = null
    private inline fun withHdcMem(block:(HDC?)->Unit){
        val ps = nativeHeap.alloc<PAINTSTRUCT>()
        val hdc = BeginPaint(hWnd,ps.ptr)
        if (bitmap == null) bitmap = CreateCompatibleBitmap(hdc, size.cx, size.cy)
        val hdcMem = CreateCompatibleDC(hdc).apply {
            SetTextColor(this, BLUE) // 字体颜色
            SetBkMode(this, TRANSPARENT) // 字体背景
        }
        SelectObject(hdcMem, bitmap)
        block(hdcMem)
        DeleteObject(hdcMem)
        EndPaint(hWnd,ps.ptr)
    }

    fun updateWindow() {
        if(invalidButtons.isNotEmpty()){
            withHdcMem{ hdcMem ->
                drawButtons(hdcMem, invalidButtons)
                invalidButtons.clear()
                UpdateLayeredWindow(hWnd, null, null, size.ptr, hdcMem, pt.ptr, 0u, blendFunction.ptr, (ULW_ALPHA or ULW_COLORKEY).toUInt())
            }
        }
    }

    fun destruct(){
        nativeHeap.free(blendFunction)
        nativeHeap.free(drawArea)
        DeleteObject(colorBrushDark)
        DeleteObject(colorBrushBright)
        DeleteObject(textBrush)
    }
}