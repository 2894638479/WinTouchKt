package wrapper

import button.Rect
import kotlinx.cinterop.*
import libs.Clib.*


@OptIn(ExperimentalForeignApi::class)
private val paramBuffer: d2dDrawParaBuffer = nativeHeap.alloc()

@OptIn(ExperimentalForeignApi::class)
private inline fun d2dDrawRectPara.set(target:D2dTarget,brush: D2dBrush, l:Float, t:Float, r:Float, b:Float) = apply {
    this.target = target.value
    this.brush = brush.value
    this.l = l
    this.t = t
    this.r = r
    this.b = b
}
@OptIn(ExperimentalForeignApi::class)
private inline fun d2dDrawRoundPara.set(target:D2dTarget, brush: D2dBrush, x:Float,y:Float,rx:Float,ry:Float) = apply {
    this.target = target.value
    this.brush = brush.value
    this.x = x
    this.y = y
    this.rx = rx
    this.ry = ry
}

@OptIn(ExperimentalForeignApi::class)
fun D2dTarget.d2dDrawRect(brush: D2dBrush, l:Float, t:Float, r:Float, b:Float,outlineWidth:Float){
    d2dDrawRect(paramBuffer.rect.set(this,brush, l, t, r, b).ptr,outlineWidth)
}

@OptIn(ExperimentalForeignApi::class)
fun D2dTarget.d2dFillRect(brush: D2dBrush, l:Float, t:Float, r:Float, b:Float){
    d2dFillRect(paramBuffer.rect.set(this,brush, l, t, r, b).ptr)
}

@OptIn(ExperimentalForeignApi::class)
fun D2dTarget.d2dDrawRoundedRect(brush: D2dBrush, l:Float, t:Float, r:Float, b:Float,rx:Float,ry:Float,outlineWidth:Float){
    d2dDrawRoundedRect(paramBuffer.rect.set(this,brush, l, t, r, b).ptr,rx, ry, outlineWidth)
}

@OptIn(ExperimentalForeignApi::class)
fun D2dTarget.d2dFillRoundedRect(brush: D2dBrush, l:Float, t:Float, r:Float, b:Float,rx:Float,ry:Float){
    d2dFillRoundedRect(paramBuffer.rect.set(this,brush, l, t, r, b).ptr,rx, ry)
}

@OptIn(ExperimentalForeignApi::class)
fun D2dTarget.d2dDrawRound(brush: D2dBrush, x:Float,y:Float,rx:Float,ry:Float,outlineWidth:Float){
    d2dDrawRound(paramBuffer.round.set(this,brush, x, y, rx, ry).ptr,outlineWidth)
}

@OptIn(ExperimentalForeignApi::class)
fun D2dTarget.d2dFillRound(brush: D2dBrush, x:Float,y:Float,rx:Float,ry:Float){
    d2dFillRound(paramBuffer.round.set(this,brush, x, y, rx, ry).ptr)
}

@OptIn(ExperimentalForeignApi::class)
fun D2dTarget.d2dDrawText(brush: D2dBrush,font:D2dFont, bound:Rect, string: String){
    if(string.isBlank()) return
    val par = paramBuffer.rect.set(this,brush,bound.left,bound.top,bound.right,bound.bottom).ptr
    d2dPushClip(par,true)
    d2dDrawText(par,font.value,string.wcstr)
    d2dPopClip(value)
}