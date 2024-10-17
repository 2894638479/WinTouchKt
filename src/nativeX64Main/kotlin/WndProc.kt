import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import platform.windows.*
import touch.mouseInput
import touch.pointerInput

const val WM_POINTERDOWN = 0x0246
const val WM_POINTERUP = 0x0247
const val WM_POINTERUPDATE = 0x0245

@OptIn(ExperimentalForeignApi::class)
fun WndProc(hWnd: HWND?, uMsg: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT {
    when (uMsg.toInt()) {
        WM_CREATE -> {
            SetWindowLongPtr!!(hWnd, GWL_STYLE, (WS_POPUP or WS_VISIBLE.toUInt()).toLong())
            SetWindowPos(hWnd, HWND_TOPMOST, 0, 0, GetSystemMetrics(SM_CXSCREEN), GetSystemMetrics(SM_CYSCREEN), SWP_SHOWWINDOW.toUInt())
            InvalidateRect(hWnd,null,TRUE)
        }
        WM_DESTROY -> { PostQuitMessage(0) }
        WM_PAINT -> { drawScope.updateWindow() }
        WM_ERASEBKGND -> {  }

//        WM_POINTERDOWN -> {}
//        WM_POINTERUPDATE -> {}
//        WM_POINTERUP -> {}
//        WM_TOUCH -> {}

        WM_POINTERDOWN -> pointerInput(wParam) { mainContainer.down(it) }
        WM_POINTERUPDATE -> pointerInput(wParam) { mainContainer.move(it) }
        WM_POINTERUP -> pointerInput(wParam) { mainContainer.up(it) }

//        WM_TOUCH -> memScoped {
//            val inputs:TouchInfos = alloc<TouchInfos>().apply { touchInput(lParam,wParam,this.ptr) }
//            for(i in 0..<inputs.size){
//                val flag = inputs.flags!!.get(i)
//                val info = inputs.infos!!.get(i)
//                when(flag.toInt()) {
//                    26 -> {
//                        mainContainer.down(info)
//                        println("down")
//                    }
//                    25 -> {
//                        mainContainer.move(info)
//                        println("move")
//                    }
//                    20 -> {
//                        mainContainer.up(info)
//                        println("up")
//                    }
//                    else -> {  }
//                }
//                println(flag)
//            }
//            destructTouchInfos(inputs.ptr)
//        }

//        WM_MOUSEMOVE  -> {
//            println("move")
//        }
//        WM_LBUTTONDOWN -> {
//            println("down")
//        }
//        WM_LBUTTONUP -> {
//            println("up")
//        }
        WM_LBUTTONDOWN -> {
            SetCapture(hWnd)
            mouseInput(lParam) { mainContainer.down(it) }
        }
        WM_MOUSEMOVE -> mouseInput(lParam) { mainContainer.move(it) }
        WM_LBUTTONUP -> {
            ReleaseCapture()
            mouseInput(lParam) { mainContainer.up(it) }
        }
        else -> return DefWindowProcW(hWnd, uMsg, wParam, lParam)
    }
    return 0
}