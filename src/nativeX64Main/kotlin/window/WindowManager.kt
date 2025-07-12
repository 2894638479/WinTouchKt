package window

import kotlinx.cinterop.*
import logger.info
import platform.windows.*
import wrapper.Hwnd
import wrapper.guiWindowClass
import wrapper.wndProcGui


private const val classNameLayered = "WinTouchKt_Layered"

@OptIn(ExperimentalForeignApi::class)
private fun registerWindowClass(className:String,block:WNDCLASSEXW.()->Unit) = memScoped {
    alloc<WNDCLASSEXW>().apply {
        cbSize = sizeOf<WNDCLASSEXW>().toUInt()
        style = (CS_HREDRAW or CS_VREDRAW).toUInt()
        lpfnWndProc = null
        cbClsExtra = 0
        cbWndExtra = 0
        hInstance = GetModuleHandleW(null)
        hIcon = LoadIcon!!(null, IDI_APPLICATION)
        hCursor = LoadCursor!!(null, IDC_ARROW)
        hbrBackground = GetStockObject(0) as HBRUSH?
        lpszMenuName = null
        lpszClassName = className.wcstr.ptr
        hIconSm = LoadIcon!!(null, IDI_APPLICATION)
    }.apply(block).run { RegisterClassExW(ptr) }
}


@OptIn(ExperimentalForeignApi::class)
fun registerLayered() = registerWindowClass(classNameLayered){
    lpfnWndProc = staticCFunction(::wndProcLayered)
    hbrBackground = GetStockObject(4) as HBRUSH?
}

@OptIn(ExperimentalForeignApi::class)
fun registerGui() = registerWindowClass(guiWindowClass){
    lpfnWndProc = staticCFunction(::wndProcGui)
}




@OptIn(ExperimentalForeignApi::class)
fun buttonsLayeredWindow(windowName:String): Hwnd {
    val hwnd = CreateWindowExW(
        (WS_EX_TOOLWINDOW or WS_EX_LAYERED or WS_EX_NOACTIVATE or WS_EX_TOPMOST).toUInt(),
        classNameLayered, windowName, WS_OVERLAPPEDWINDOW.toUInt(),
        CW_USEDEFAULT, CW_USEDEFAULT, 0,0,null,null, GetModuleHandleW(null), null
    )
    RegisterTouchWindow(hwnd, (TWF_WANTPALM or TWF_FINETOUCH).toUInt())
    if(hwnd == null) error("buttons layered window create failed")
    return Hwnd(hwnd)
}


@OptIn(ExperimentalForeignApi::class)
fun showWindow(hwnd:Hwnd){
    ShowWindow(hwnd.HWND, SW_SHOW)
    UpdateWindow(hwnd.HWND)
}


@OptIn(ExperimentalForeignApi::class)
fun loopWindowMessage(hwnd: Hwnd? = null) = memScoped {
    val message: MSG = alloc()
    while (GetMessageW(message.ptr, hwnd?.HWND, 0u, 0u) > 0) {
        info("message looped once")
        TranslateMessage(message.ptr)
        DispatchMessageW(message.ptr)
    }
}