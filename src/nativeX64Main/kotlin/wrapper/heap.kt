package wrapper

import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
inline fun <reified T: CStructVar> alloc() = nativeHeap.alloc<T>()

@OptIn(ExperimentalForeignApi::class)
fun <T: CStructVar> T.free() = nativeHeap.free(this)