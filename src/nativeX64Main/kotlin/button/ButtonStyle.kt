package button

import draw.*
import error.infoBox
import kotlinx.cinterop.ExperimentalForeignApi


@OptIn(ExperimentalForeignApi::class)
class ButtonStyle(
    pressed:Boolean,
    scale:Float,
    color: Color?,
    textColor: Color?,
    outlineColor: Color?,
    outlineWidth: Float?,
    fontFamily:String?,
    fontSize:Float?,
    fontStyle:String?,
    fontWeight:Int?,
){
    val font = Store.font(Font(fontFamily,fontSize,fontStyle,fontWeight,scale))
    val brushText = Store.brush(textColor ?: RED)
    val brush = Store.brush(color ?: if(pressed) GREY_BRIGHT else GREY_DARK)
    val brushOutline = Store.brush(outlineColor ?: WHITE)
    val outlineWidth = outlineWidth?.let { if(it>0) it else 0f } ?: 0f
}