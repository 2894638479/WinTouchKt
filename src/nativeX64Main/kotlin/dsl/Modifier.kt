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
    minW:Int,
    minH:Int
){
    internal var minW = minW
        get() = max(field,width)
    internal var minH = minH
        get() = max(field,height)
    constructor():this(0,0,0f,0,0,0,0,0,0)
    internal val paddingW get() = paddingLeft + paddingRight
    internal val paddingH get() = paddingTop + paddingBottom
}
val M get() = Modifier()

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

fun Modifier.padding(h:Int = 0, v:Int = 0) = apply {
    paddingRight = h
    paddingLeft = h
    paddingBottom = v
    paddingTop = v
}

fun Modifier.padding(left:Int = 0, top:Int = 0, right:Int = 0, bottom:Int = 0) = apply {
    paddingLeft += left
    paddingTop += top
    paddingRight += right
    paddingBottom += bottom
}