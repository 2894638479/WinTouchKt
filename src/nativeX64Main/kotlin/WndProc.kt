import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import platform.windows.*
import touch.mouseInput
import touch.pointerInput
import touch.toEvent

const val WM_POINTERDOWN = 0x0246
const val WM_POINTERUP = 0x0247
const val WM_POINTERUPDATE = 0x0245

@OptIn(ExperimentalForeignApi::class)
fun wndProc(hWnd: HWND?, uMsg: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT {
    fun default() = DefWindowProcW(hWnd, uMsg, wParam, lParam)
    when (uMsg.toInt()) {
        WM_CREATE -> {
            SetWindowLongPtr!!(hWnd, GWL_STYLE, (WS_POPUP or WS_VISIBLE.toUInt()).toLong())
            SetWindowPos(hWnd, HWND_TOPMOST, 0, 0, GetSystemMetrics(SM_CXSCREEN), GetSystemMetrics(SM_CYSCREEN), SWP_SHOWWINDOW.toUInt())
        }
        WM_PAINT -> {
            drawScopeNullable?.onDraw()
            ValidateRect(hWnd,null)
        }
        WM_SIZE -> {
            drawScopeNullable?.resize()
        }
        WM_ERASEBKGND -> {  }
        WM_POINTERDOWN -> pointerInput(wParam) { mainContainer.down(it.toEvent()) }
        WM_POINTERUPDATE -> pointerInput(wParam) { mainContainer.move(it.toEvent()) }
        WM_POINTERUP -> pointerInput(wParam) { mainContainer.up(it.toEvent()) }
        WM_LBUTTONDOWN -> {
            SetCapture(hWnd)
            mouseInput(lParam) { mainContainer.down(it.toEvent()) }
        }
        WM_MOUSEMOVE -> mouseInput(lParam) {
            mainContainer.move(it.toEvent())
        }
        WM_LBUTTONUP -> {
            ReleaseCapture()
            mouseInput(lParam) { mainContainer.up(it.toEvent()) }
        }
        else -> {
            return default()
        }
    }
    return 0
}