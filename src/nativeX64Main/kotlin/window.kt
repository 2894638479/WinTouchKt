import error.errorBox
import error.infoBox
import kotlinx.cinterop.*
import libs.Clib.hwndHolder
import libs.Clib.windowStep1
import libs.Clib.windowStep2
import libs.Clib.windowStep3
import platform.windows.*

const val className = "WinTouchKt"
const val windowName = "winTouch"

@OptIn(ExperimentalForeignApi::class)
fun window(onInitialized:( CPointer<hwndHolder>?)->Unit):Unit = memScoped {
//    val hInstance = GetModuleHandle!!(null)
//    val wcex : WNDCLASSEXW = alloc()
//    wcex.apply {
//        cbSize = sizeOf<WNDCLASSEXW>().toUInt()
//        lpfnWndProc = staticCFunction(::wndProc)
//        style = 0u
//        cbClsExtra = 0
//        cbWndExtra = 0
//        this.hInstance = hInstance
//        hIcon = LoadIcon!!(null, IDI_APPLICATION)
//        hCursor = LoadCursor!!(null, IDC_ARROW)
//        hbrBackground = CreateSolidBrush(TRANSPARENT.toUInt())
//        lpszMenuName = null
//        lpszClassName = className.wcstr.ptr
//        hIconSm = LoadIcon!!(null, IDI_APPLICATION)
//    }
//    val s = RegisterClassExW(wcex.ptr).toString()
//    infoBox(s)
//    val hwnd = CreateWindowExW(
//        (WS_EX_TOOLWINDOW or WS_EX_LAYERED or WS_EX_NOACTIVATE or WS_EX_TOPMOST).toUInt(),
//        "WinTouchKt",
//        "WinTouchKt",
//        WS_OVERLAPPEDWINDOW.toUInt(),
//        CW_USEDEFAULT, CW_USEDEFAULT, 0,0,null,null, hInstance, null
//    )
//    infoBox("2")
//    RegisterTouchWindow(hwnd, (TWF_WANTPALM or TWF_FINETOUCH).toUInt())
//    onInitialized(hwnd?.reinterpret())
//    ShowWindow(hwnd, SW_SHOW)
//    UpdateWindow
    infoBox("1")
    windowStep1().let { if(it != 0) errorBox("窗口初始化1错误")}
    infoBox("2")
    val hwnd = windowStep2() ?: errorBox("hwnd为null")
    infoBox("3")
    onInitialized(hwnd)
    windowStep3(hwnd)
    val message: MSG = alloc()
    while (GetMessageW(message.ptr, null, 0u, 0u) > 0) {
        TranslateMessage(message.ptr)
        DispatchMessageW(message.ptr)
    }
}