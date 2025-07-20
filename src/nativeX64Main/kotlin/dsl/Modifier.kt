package dsl

import kotlin.math.max

class Modifier internal constructor(
    internal var width:Int,
    internal var height:Int,
    internal var weight:Float,
    internal var paddingTop:Int,
    internal var paddingLeft:Int,
    internal var paddingBottom:Int,
    internal var paddingRight:Int,
    internal var minW:Int,
    internal var minH:Int
){
    constructor():this(0,0,1f,0,0,0,0,0,0)
    internal val fullWidth get() = width + paddingLeft + paddingRight
    internal val fullHeight get() = height + paddingTop + paddingBottom
    internal val fullMinW get() = minW + paddingLeft + paddingRight
    internal val fullMinH get() = minH + paddingTop + paddingBottom
    internal val layoutMinW get() = max(minW,width) + paddingLeft + paddingRight
    internal val layoutMinH get() = max(minH,height) + paddingBottom + paddingBottom
}

fun Modifier.size(width: Int, height: Int) = apply { this.width = width; this.height = height }
fun Modifier.width(width: Int) = apply { this.width = width }
fun Modifier.height(height: Int) = apply { this.height = height }
fun Modifier.min(width: Int, height: Int) = apply { this.minW = width; this.minH = height }
fun Modifier.minWidth(width: Int) = apply { this.minW = width }
fun Modifier.minHeight(height: Int) = apply { this.minH = height }
fun Modifier.padding(value:Int) = apply {
    paddingLeft = value
    paddingTop = value
    paddingRight = value
    paddingBottom = value
}

fun Modifier.padding(horizontal:Int = 0, vertical:Int = 0) = apply {
    paddingRight = horizontal
    paddingLeft = horizontal
    paddingBottom = vertical
    paddingTop = vertical
}

fun Modifier.padding(left:Int = 0, top:Int = 0, right:Int = 0, bottom:Int = 0) = apply {
    paddingLeft = left
    paddingTop = top
    paddingRight = right
    paddingBottom = bottom
}