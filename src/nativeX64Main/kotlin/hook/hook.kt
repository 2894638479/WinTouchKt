package hook

import kotlinx.cinterop.*
import platform.windows.*


@OptIn(ExperimentalForeignApi::class)
private var hook: HHOOK? = null

@OptIn(ExperimentalForeignApi::class)
fun setHook(hInstance:HMODULE?){
    //hook = SetWindowsHookEx!!(WH_MOUSE_LL, staticCFunction(::hookMouseProc), hInstance, 0u)
}

@OptIn(ExperimentalForeignApi::class)
private fun hookMouseProc(nCode:Int, wParam:WPARAM, lParam:LPARAM):LRESULT{
    if (nCode >= 0) {
        when(wParam.toInt()){
            WM_LBUTTONDOWN -> {
            }
            WM_LBUTTONUP -> {
            }
            WM_MOUSEMOVE -> {
            }
            else -> return CallNextHookEx(hook, nCode, wParam, lParam)
        }
        return 1
    }
    return CallNextHookEx(hook, nCode, wParam, lParam)
}