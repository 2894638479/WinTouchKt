package draw

import kotlinx.cinterop.*
import libs.Clib.*

@OptIn(ExperimentalForeignApi::class)
val paramBuffer:d2dDrawParaBuffer = nativeHeap.alloc()