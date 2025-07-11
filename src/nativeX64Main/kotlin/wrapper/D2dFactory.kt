package wrapper

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
                val result = d2dCreateFactory(ptr)
                if (result != 0) error("d2d factory initialize failed $result")
            }.value?.let { D2dFactory(it) } ?: error("d2dFactory is null")
        }
    }
}