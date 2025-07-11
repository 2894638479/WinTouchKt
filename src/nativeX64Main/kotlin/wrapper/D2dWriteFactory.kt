package wrapper

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
                val result = d2dCreateWriteFactory(ptr)
                if(result != 0) error("d2dWriteFactory create failed $result")
            }.value?.let { D2dWriteFactory(it) } ?: error("d2dWriteFactory is null")
        }
    }
}