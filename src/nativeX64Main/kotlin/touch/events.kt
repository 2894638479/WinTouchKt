package touch

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import libs.Clib.TouchInfo
import libs.Clib.getMouseX
import libs.Clib.getMouseY
import libs.Clib.getTouchInfo
import platform.windows.LPARAM
import platform.windows.WPARAM


@OptIn(ExperimentalForeignApi::class)
inline fun pointerInput(wParam: WPARAM,event:(TouchInfo)->Unit) = memScoped {
    alloc<TouchInfo>().apply {
        getTouchInfo(wParam,ptr)
        event(this)
    }
}

@OptIn(ExperimentalForeignApi::class)
inline fun mouseInput(lParam: LPARAM,event:(TouchInfo)->Unit) = memScoped {
    alloc<TouchInfo>().apply {
        pointX = getMouseX(lParam)
        pointY = getMouseY(lParam)
        id = 1u
        event(this)
    }
}