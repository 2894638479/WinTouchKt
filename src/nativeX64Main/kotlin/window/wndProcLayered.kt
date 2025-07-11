package window

import container.Container
import error.catchInKotlin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.reinterpret
import logger.info
import platform.windows.*
import touch.mouseInput
import touch.pointerInput
import touch.toEvent
import wrapper.Hwnd

const val WM_POINTERDOWN = 0x0246
const val WM_POINTERUP = 0x0247
const val WM_POINTERUPDATE = 0x0245

@OptIn(ExperimentalForeignApi::class)
fun wndProcLayered(hWnd: HWND?, uMsg: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT = catchInKotlin {
    val container = Container.hwndContainer(Hwnd(hWnd?.reinterpret() ?: error("hwnd is null")))
    val drawScope = container?.drawScope
    fun default() = DefWindowProcW(hWnd, uMsg, wParam, lParam)
    ValidateRect(hWnd,null)
    info("wndProcLayered umsg $uMsg")
    when (uMsg.toInt()) {
        WM_CREATE -> {
            SetWindowLongPtr!!(hWnd, GWL_STYLE, (WS_POPUP or WS_VISIBLE.toUInt()).toLong())
            SetWindowPos(hWnd, HWND_TOPMOST, 0, 0, GetSystemMetrics(SM_CXSCREEN), GetSystemMetrics(SM_CYSCREEN), SWP_SHOWWINDOW.toUInt())
        }
        WM_CLOSE -> {
            DestroyWindow(hWnd)
        }
        WM_DESTROY -> {
            container?.destroy()
        }
        WM_PAINT -> {
            drawScope?.onDraw()?.let { ValidateRect(hWnd,null) }
        }
        WM_SIZE -> {
            drawScope?.resize()
        }
        WM_ERASEBKGND -> {  }
        WM_POINTERDOWN -> pointerInput(wParam) { container?.down(it.toEvent()) }
        WM_POINTERUPDATE -> pointerInput(wParam) {
            container?.move(it.toEvent())
        }
        WM_POINTERUP -> pointerInput(wParam) { container?.up(it.toEvent()) }
        WM_LBUTTONDOWN -> {
            SetCapture(hWnd)
            mouseInput(lParam) { container?.down(it.toEvent()) }
        }
        WM_MOUSEMOVE -> mouseInput(lParam) {
            container?.move(it.toEvent())
        }
        WM_LBUTTONUP -> {
            ReleaseCapture()
            mouseInput(lParam) { container?.up(it.toEvent()) }
        }
        else -> {
            return default()
        }
    }
    return default()
}