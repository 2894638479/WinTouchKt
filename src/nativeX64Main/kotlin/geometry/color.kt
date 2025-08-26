package geometry

import kotlinx.serialization.Serializable

val RED = Color(255u,0u,0u)
val GREEN = Color(0u,255u,0u)
val BLUE = Color(0u,0u,255u)
val BLACK = Color(0u,0u,0u)
val WHITE = Color(255u,255u,255u)
val GREY_BRIGHT = Color(200u,200u,200u)
val GREY_DARK = Color(50u,50u,50u)

@Serializable
data class Color(
    val r:UByte,
    val g:UByte,
    val b:UByte
){
    fun setR(value: UByte) = Color(value,g,b)
    fun setG(value: UByte) = Color(r,value,b)
    fun setB(value: UByte) = Color(r,g,value)
    fun toInt() = r.toInt() or (g.toInt() shl 8) or(b.toInt() shl 16)
    fun toUInt() = toInt().toUInt()
}