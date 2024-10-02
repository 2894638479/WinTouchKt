package sendInput

import kotlinx.cinterop.*
import platform.windows.*

@OptIn(ExperimentalForeignApi::class)
inline fun sendInput(inputInfo:INPUT.()->Unit) = memScoped {
    val input:INPUT = alloc<INPUT>().apply {
        inputInfo()
    }
    SendInput(1u,input.ptr, sizeOf<INPUT>().toInt())
}