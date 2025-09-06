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
    constructor():this(0,0,1f,0,0,0,0,0,0)
    internal val paddingW get() = paddingLeft + paddingRight
    internal val paddingH get() = paddingTop + paddingBottom
}
val M get() = Modifier()

fun Modifier.size(width: Int, height: Int) = apply { this.width = width; this.height = height }
fun Modifier.width(width: Int) = apply { this.width = width }
fun Modifier.height(height: Int) = apply { this.height = height }
fun Modifier.minWidth(width: Int) = apply { minW = max(minW,width) }
fun Modifier.minHeight(height: Int) = apply { minH = max(minH,height) }
fun Modifier.minSize(width: Int,height: Int) = minWidth(width).minHeight(height)
fun Modifier.padding(value:Int) = padding(value,value,value,value)

fun Modifier.padding(h:Int = 0, v:Int = 0) = padding(h,v,h,v)

fun Modifier.padding(left:Int = 0, top:Int = 0, right:Int = 0, bottom:Int = 0) = apply {
    paddingLeft += left
    paddingTop += top
    paddingRight += right
    paddingBottom += bottom
}