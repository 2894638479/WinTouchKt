import hook.setHook
import kotlinx.cinterop.*
import platform.windows.*

const val className = "MyWindow"
const val windowName = "window"

@OptIn(ExperimentalForeignApi::class)
fun window(onInitialized:(HWND?)->Unit):Unit = memScoped {
    val hInstance = GetModuleHandle!!(null)
    setHook(hInstance)

    val wcex : WNDCLASSEXW = alloc()
    wcex.apply {
        cbSize = sizeOf<WNDCLASSEXW>().toUInt()
        lpfnWndProc = staticCFunction(::WndProc)
        style = 0u
        cbClsExtra = 0
        cbWndExtra = 0
        this.hInstance = hInstance
        hIcon = LoadIcon!!(null, IDI_APPLICATION)
        hCursor = LoadCursor!!(null, IDC_ARROW)
        hbrBackground = CreateSolidBrush(TRANSPARENT.toUInt())
        lpszMenuName = null
        lpszClassName = className.wcstr.ptr
        hIconSm = LoadIcon!!(null, IDI_APPLICATION)
    }
    RegisterClassExW(wcex.ptr)

    val hwnd =CreateWindowEx!!(
        (WS_EX_TOOLWINDOW or WS_EX_LAYERED or WS_EX_NOACTIVATE or WS_EX_TOPMOST).toUInt(),
        className.wcstr.ptr,
        windowName.wcstr.ptr,
        WS_OVERLAPPEDWINDOW.toUInt(),
        CW_USEDEFAULT, CW_USEDEFAULT, 240, 120,
        null,null, hInstance, null
    )
    onInitialized(hwnd)
    ShowWindow(hwnd, SW_SHOW)
    UpdateWindow(hwnd)

    val message: MSG = alloc()
    while (GetMessageW(message.ptr, null, 0u, 0u) > 0) {
        TranslateMessage(message.ptr)
        DispatchMessageW(message.ptr)
    }
}