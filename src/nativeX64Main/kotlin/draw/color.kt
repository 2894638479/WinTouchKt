package draw

import button.Button
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import platform.windows.COLORREF

fun rgb(r: UByte, g: UByte, b: UByte): COLORREF {
    return (r.toUInt() or (g.toUInt() shl 8) or (b.toUInt() shl 16))
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
val Button.rectC:Color get() {
    return if(pressed){
        colorPressed ?: GREY_BRIGHT
    } else {
        color ?: GREY_DARK
    }
}

@Serializable
data class Color(
    val r:UByte,
    val g:UByte,
    val b:UByte
){
    @Transient
    val ref = rgb(r,g,b)
}