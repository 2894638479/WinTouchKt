package hook

import WM_POINTERDOWN
import WM_POINTERUP
import WM_POINTERUPDATE
import kotlinx.cinterop.*
import mainContainer
import platform.windows.*
import touch.pointerInput


@OptIn(ExperimentalForeignApi::class)
private var hook: HHOOK? = null
@OptIn(ExperimentalForeignApi::class)
val hooked get() = hook != null

@OptIn(ExperimentalForeignApi::class)
fun setHook(hInstance:HMODULE?){
    hook = SetWindowsHookEx!!(WH_CALLWNDPROC, staticCFunction(::hookProc), hInstance, 0u)
    println("startHook")
}

@OptIn(ExperimentalForeignApi::class)
fun unHook(){
    UnhookWindowsHookEx(hook)
    hook = null
}

@OptIn(ExperimentalForeignApi::class)
private fun hookProc(nCode:Int, wParam:WPARAM, lParam:LPARAM):LRESULT{
    println("hook success")
    if (true) {
        when(wParam.toInt()){
            WM_POINTERDOWN -> pointerInput(wParam) {
                println("hookDown")
                if(mainContainer.down(it)) return 1
            }
            WM_POINTERUPDATE -> pointerInput(wParam) {
                println("hookMove")
                if(mainContainer.move(it)) return 1
            }
            WM_POINTERUP -> pointerInput(wParam) {
                if(mainContainer.up(it)) return 1
            }
            else -> return CallNextHookEx(hook, nCode, wParam, lParam)
        }
        return CallNextHookEx(hook, nCode, wParam, lParam)
    }
    return CallNextHookEx(hook, nCode, wParam, lParam)
}