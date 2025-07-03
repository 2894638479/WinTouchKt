package wrapper

import error.direct2dInitializeError
import kotlinx.cinterop.*
import libs.Clib.d2dCreateWriteFactory
import libs.Clib.d2dFreeWriteFactory
import libs.Clib.d2dWriteFactoryHolder

@OptIn(ExperimentalForeignApi::class)
value class D2dWriteFactory(val value: CPointer<d2dWriteFactoryHolder>){
    fun free() = d2dFreeWriteFactory(value)
    companion object {
        fun create():D2dWriteFactory = memScoped {
            alloc<CPointerVar<d2dWriteFactoryHolder>>().apply {
                if(d2dCreateWriteFactory(ptr) != 0) direct2dInitializeError()
            }.value?.let { D2dWriteFactory(it) } ?: direct2dInitializeError()
        }
    }
}