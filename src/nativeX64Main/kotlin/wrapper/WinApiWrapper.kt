package wrapper

import geometry.Rect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.windows.RECT
import platform.windows.WINBOOL
import kotlin.math.roundToInt


fun WINBOOL.ifFalse(block:()->Unit){if(this == 0) block()}


fun RECT.padding(value:Int){
    left += value
    top += value
    right -= value
    bottom -= value
}
fun RECT.toOrigin(){
    right -= left
    bottom -= top
    left = 0
    top = 0
}
fun RECT.copyFrom(other:RECT){
    left = other.left
    top = other.top
    right = other.right
    bottom = other.bottom
}

val RECT.width get() = right - left
val RECT.height get() = bottom - top
val RECT.midX get() = (left + right) / 2
val RECT.midY get() = (top + bottom) / 2

@OptIn(ExperimentalForeignApi::class)
fun allocRECT(block:RECT.()->Unit) = memScoped { alloc<RECT>().block() }

fun RECT.cutTop(rate:Float) { top += ((bottom - top)*rate).roundToInt() }
fun RECT.cutBottom(rate:Float) { bottom -= ((bottom - top)*rate).roundToInt() }
fun RECT.cutLeft(rate:Float) { left += ((right - left)*rate).roundToInt() }
fun RECT.cutRight(rate:Float) { right -= ((right - left)*rate).roundToInt() }

fun RECT.toRect() = Rect(left.toFloat(),top.toFloat(),right.toFloat(),bottom.toFloat())
fun RECT.str() = "$left $top $right $bottom"