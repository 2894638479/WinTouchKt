package wrapper

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret
import libs.Clib.hwndHolder
import platform.windows.HWND

@OptIn(ExperimentalForeignApi::class)
value class Hwnd(val value:CPointer<hwndHolder>){
    val HWND:HWND get() = value.reinterpret()
}