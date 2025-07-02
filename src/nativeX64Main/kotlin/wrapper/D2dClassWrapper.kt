package wrapper

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import libs.Clib.*


@OptIn(ExperimentalForeignApi::class)
value class D2dFont(val value: CPointer<d2dTextFormatHolder>){
    fun free() = d2dFreeTextFormat(value)
}

@OptIn(ExperimentalForeignApi::class)
value class D2dBrush(val value: CPointer<d2dSolidColorBrushHolder>){
    fun free() = d2dFreeSolidColorBrush(value)
}

@OptIn(ExperimentalForeignApi::class)
value class D2dTarget(val value:CPointer<d2dTargetHolder>){
    fun free() = d2dFreeTarget(value)
}

@OptIn(ExperimentalForeignApi::class)
value class D2dWriteFactory(val value: CValuesRef<d2dWriteFactoryHolder>){
    fun free() = d2dFreeWriteFactory(value)
}