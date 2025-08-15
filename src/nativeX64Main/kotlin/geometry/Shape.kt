package geometry

import wrapper.D2dBrush
import wrapper.D2dTarget


interface Shape {
    fun containPointByRelativePos(x: Float, y: Float):Boolean
    context(offset:Point)
    fun d2dDraw(target: D2dTarget,brush: D2dBrush,width:Float)
    context(offset:Point)
    fun d2dFill(target: D2dTarget,brush: D2dBrush)
    fun rescaled(scale:Float): Shape
    fun padding(value:Float): Shape?
    val innerRect: Rect
    val outerRect: Rect
    val isValid:Boolean

    context(offset: Point)
    operator fun contains(point: Point) = (point - offset).run { containPointByRelativePos(x,y) }
}