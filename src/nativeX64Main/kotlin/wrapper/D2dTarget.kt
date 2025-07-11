package wrapper

import kotlinx.cinterop.*
import libs.Clib.*


@OptIn(ExperimentalForeignApi::class)
value class D2dTarget(val value:CPointer<d2dTargetHolder>){
    fun free() = d2dFreeTarget(value)
    companion object {
        fun create(hwnd:Hwnd,factory:D2dFactory):D2dTarget = memScoped {
            alloc<CPointerVar<d2dTargetHolder>>().apply {
                val result = d2dCreateTarget(factory.value,ptr,hwnd.value.reinterpret())
                if(result != 0) error("d2dTarget create failed $result")
                //关闭抗锯齿，避免边缘带线
                d2dSetAntialiasMode(value,false)
            }.value?.let { D2dTarget(it) } ?: error("d2dTarget is null")
        }
    }
}

