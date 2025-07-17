package geometry

import wrapper.D2dBrush
import wrapper.D2dTarget


interface Shape {
    fun containPoint(x: Float, y: Float):Boolean
    fun d2dDraw(target: D2dTarget,brush: D2dBrush,width:Float)
    fun d2dFill(target: D2dTarget,brush: D2dBrush)
    fun rescaled(scale:Float): Shape
    fun offset(offset: Point): Shape
    fun padding(width:Float): Shape?
    val innerRect: Rect
    val outerRect: Rect
    val isValid:Boolean
}