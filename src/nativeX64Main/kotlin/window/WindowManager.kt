package window

import kotlinx.cinterop.*
import logger.info
import platform.windows.*
import wrapper.Hwnd

class WindowManager {
    companion object {
        private const val classNameLayered = "WinTouchKt_Layered"
        @OptIn(ExperimentalForeignApi::class)
        private fun registerWindowClass(className:String,block:WNDCLASSEXW.()->Unit) = memScoped {
            alloc<WNDCLASSEXW>().apply {
                cbSize = sizeOf<WNDCLASSEXW>().toUInt()
                style = 0u
                cbClsExtra = 0
                cbWndExtra = 0
                this.hInstance = GetModuleHandleW(null)
                hIcon = LoadIcon!!(null, IDI_APPLICATION)
                hCursor = null
                hbrBackground = GetStockObject(4) as HBRUSH?
                lpszMenuName = null
                lpszClassName = className.wcstr.ptr
                hIconSm = LoadIcon!!(null, IDI_APPLICATION)
            }.apply(block).run { RegisterClassExW(ptr) }
        }
        @OptIn(ExperimentalForeignApi::class)
        fun registerLayered() = registerWindowClass(classNameLayered){
            lpfnWndProc = staticCFunction(::wndProcLayered)
        }

        @OptIn(ExperimentalForeignApi::class)
        fun buttonsLayeredWindow(windowName:String): Hwnd {
            val hwnd = CreateWindowExW(
                (WS_EX_TOOLWINDOW or WS_EX_LAYERED or WS_EX_NOACTIVATE or WS_EX_TOPMOST).toUInt(),
                classNameLayered, windowName, WS_OVERLAPPEDWINDOW.toUInt(),
                CW_USEDEFAULT, CW_USEDEFAULT, 0,0,null,null, GetModuleHandleW(null), null
            )
            RegisterTouchWindow(hwnd, (TWF_WANTPALM or TWF_FINETOUCH).toUInt())
            println(2)
            return Hwnd(hwnd?.reinterpret() ?: error("hwnd is null"))
        }


        @OptIn(ExperimentalForeignApi::class)
        fun loopWindowMessage(hwnd: Hwnd) = memScoped {
            ShowWindow(hwnd.HWND, SW_SHOW)
            UpdateWindow(hwnd.HWND)
            val message: MSG = alloc()
            while (GetMessageW(message.ptr, hwnd.HWND, 0u, 0u) > 0) {
                info("message looped once")
                TranslateMessage(message.ptr)
                DispatchMessageW(message.ptr)
            }
        }
    }
}