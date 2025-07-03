package wrapper

import error.direct2dInitializeError
import kotlinx.cinterop.*
import libs.Clib.d2dCreateFactory
import libs.Clib.d2dFactoryHolder
import libs.Clib.d2dFreeFactory

@OptIn(ExperimentalForeignApi::class)
value class D2dFactory(val value: CPointer<d2dFactoryHolder>){
    fun free() = d2dFreeFactory(value)
    companion object {
        fun create(): D2dFactory = memScoped {
            alloc<CPointerVar<d2dFactoryHolder>>().apply {
                if (d2dCreateFactory(ptr) != 0) direct2dInitializeError()
            }.value?.let { D2dFactory(it) } ?: direct2dInitializeError()
        }
    }
}