package draw

import button.Button
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import platform.windows.*

fun rgb(r: UByte, g: UByte, b: UByte): COLORREF {
    return (r.toUInt() or (g.toUInt() shl 8) or (b.toUInt() shl 16))
}
fun argb(a: UByte,r: UByte, g: UByte, b: UByte): COLORREF {
    return (r.toUInt() or (g.toUInt() shl 8) or (b.toUInt() shl 16) or (a.toUInt() shl 24))
}
val RED = Color(255u,0u,0u)
val GREEN = Color(0u,255u,0u)
val BLUE = Color(0u,0u,255u)
val BLACK = Color(0u,0u,0u)
val WHITE = Color(255u,255u,255u)
val GREY_BRIGHT = Color(200u,200u,200u)
val GREY_DARK = Color(50u,50u,50u)



val Button.textC:Color get() {
    return if(pressed){
        textColorPressed ?: RED
    } else {
        textColor ?: RED
    }
}

@OptIn(ExperimentalForeignApi::class)
val Button.brush:HBRUSH? get(){
    val color = if(pressed){
        colorPressed ?: GREY_BRIGHT
    } else {
        color ?: GREY_DARK
    }
    return getColorBrush(color.ref)
}

@OptIn(ExperimentalForeignApi::class)
private val colorBrushList = mutableMapOf<COLORREF,HBRUSH?>()
@OptIn(ExperimentalForeignApi::class)
fun getColorBrush(ref: COLORREF):HBRUSH? {
    return colorBrushList[ref] ?: run {
        CreateSolidBrush(ref)?.apply {
            colorBrushList[ref] = this
        }
    }
}
@OptIn(ExperimentalForeignApi::class)
fun deleteAllColorBrush(){
    colorBrushList.forEach {
        DeleteObject(it.value)
    }
    colorBrushList.clear()
}

@Serializable
class Color(
    val r:UByte,
    val g:UByte,
    val b:UByte
){
    @Transient
    val ref = rgb(r,g,b)
    @OptIn(ExperimentalForeignApi::class)
    val brush get() = getColorBrush(ref)
}