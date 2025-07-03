package wrapper

import error.direct2dInitializeError
import kotlinx.cinterop.*
import libs.Clib.*


@OptIn(ExperimentalForeignApi::class)
value class D2dTarget(val value:CPointer<d2dTargetHolder>){
    fun free() = d2dFreeTarget(value)
    companion object {
        fun create(hwnd:Hwnd,factory:D2dFactory):D2dTarget = memScoped {
            alloc<CPointerVar<d2dTargetHolder>>().apply {
                if(d2dCreateTarget(factory.value,ptr,hwnd.value.reinterpret()) != 0) direct2dInitializeError()
                //关闭抗锯齿，避免边缘带线
                d2dSetAntialiasMode(value,false)
            }.value?.let { D2dTarget(it) } ?: direct2dInitializeError()
        }
    }
}

