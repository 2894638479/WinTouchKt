package wrapper

import kotlinx.cinterop.*
import libs.Clib.hwndHolder
import logger.info
import logger.warning
import platform.windows.*

@OptIn(ExperimentalForeignApi::class)
value class Hwnd(val value:CPointer<hwndHolder>){
    constructor(hwnd: HWND?):this(hwnd?.reinterpret<hwndHolder>() ?: error("hwnd is null"))
    val HWND:HWND get() = value.reinterpret()
    val rect get() = rectBuffer.apply {
        GetClientRect(HWND, rectBuffer.ptr)
    }
    companion object {
        private val rectBuffer  = nativeHeap.alloc<RECT>()
    }
    fun show() = ShowWindow(HWND, SW_SHOW)
    fun hide() = ShowWindow(HWND, SW_HIDE)
    val visible get() = IsWindowVisible(HWND) != FALSE
    fun update() = UpdateWindow(HWND)
    fun showAndUpdate() { show();update() }
    fun setRect(x:Int, y:Int, w:Int, h:Int){
        info("hwnd moved to $x $y ,size  $w $h")
        MoveWindow(HWND,x,y,w,h, TRUE).ifFalse { warning("hwnd move false") }
    }
    fun close() = CloseWindow(HWND)
    fun setRect(rect: tagRECT) = rect.run { setRect(left,top,right - left,bottom - top) }
    var name:String
        get() {
            val length = GetWindowTextLengthW(HWND)
            memScoped {
                val buffer = allocArray<UShortVar>(length + 1)
                GetWindowTextW(HWND,buffer,length + 1)
                return buffer.toKStringFromUtf16()
            }
        }
        set(value) {
            SetWindowTextW(HWND, value)
        }
    val controlId get() = GetDlgCtrlID(HWND).toUShort()
    fun invalidateRect(rect:RECT? = null) = InvalidateRect(HWND,rect?.ptr,1)
    fun validateRect(rect: RECT? = null) = ValidateRect(HWND,rect?.ptr)
    inline fun scroll(bar:Int,value:tagSCROLLINFO.()->Int) = memScoped {
        val si = alloc<tagSCROLLINFO>()
        si.fMask = SIF_ALL.toUInt()
        GetScrollInfo(HWND,bar,si.ptr)
        si.fMask = SIF_POS.toUInt()
        val originPos = si.nPos
        si.nPos = si.value()
        SetScrollInfo(HWND,bar,si.ptr,TRUE)
        GetScrollInfo(HWND,bar,si.ptr)
        si.nPos - originPos
    }
    fun updateScrollSize(bar:Int,height:Int) = memScoped {
        val si = alloc<tagSCROLLINFO>()
        si.fMask = SIF_POS.toUInt()
        GetScrollInfo(HWND,bar,si.ptr)
        si.nPage = rect.height.toUInt()
        si.nMin = 0
        si.nMax = height - 1
        si.fMask = SIF_ALL.toUInt()
        SetScrollInfo(HWND,bar,si.ptr,TRUE)
        ScrollWindow(HWND,0,-si.nPos,null,null)
    }
}