package wrapper

import button.Rect
import platform.windows.RECT
import platform.windows.WINBOOL


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

fun RECT.toRect() = Rect(left.toFloat(),top.toFloat(),right.toFloat(),bottom.toFloat())
fun RECT.str() = "$left $top $right $bottom"