package wrapper

import kotlinx.cinterop.*
import libs.Clib.hwndHolder
import platform.windows.GetWindowRect
import platform.windows.HWND
import platform.windows.RECT

@OptIn(ExperimentalForeignApi::class)
value class Hwnd(val value:CPointer<hwndHolder>){
    constructor(hwnd: HWND?):this(hwnd?.reinterpret<hwndHolder>() ?: error("hwnd is null"))
    val HWND:HWND get() = value.reinterpret()
    val size get() = rectBuffer.apply {
        GetWindowRect(HWND, rectBuffer.ptr)
    }
    companion object {
        private val rectBuffer  = nativeHeap.alloc<RECT>()
    }
}