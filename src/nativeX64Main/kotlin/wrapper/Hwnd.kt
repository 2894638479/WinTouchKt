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
    val visible get() = IsWindowVisible(HWND)
    fun update() = UpdateWindow(HWND)
    fun showAndUpdate() { show();update() }
    fun setRect(x:Int, y:Int, w:Int, h:Int){
        info("hwnd moved to $x $y ,size  $w $h")
        MoveWindow(HWND,x,y,w,h,TRUE).ifFalse { warning("hwnd move false") }
    }
    fun setRect(rect: tagRECT) = rect.run { setRect(left,top,right - left,bottom - top) }
    fun getName():String{
        val length = GetWindowTextLengthW(HWND)
        memScoped {
            val buffer = allocArray<UShortVar>(length + 1)
            GetWindowTextW(HWND,buffer,length + 1)
            return buffer.toKStringFromUtf16()
        }
    }
}