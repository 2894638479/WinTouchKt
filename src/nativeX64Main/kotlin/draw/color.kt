package draw

import platform.windows.COLORREF

fun rgb(r: Int, g: Int, b: Int): COLORREF {
    return (r or (g shl 8) or (b shl 16)).toUInt()
}
val RED = rgb(255,0,0)
val GREEN = rgb(0,255,0)
val BLUE = rgb(0,0,255)