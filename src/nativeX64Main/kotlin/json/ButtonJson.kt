package json

import button.*
import error.errorBox
import error.infoBox
import error.nullPtrError
import kotlinx.serialization.Serializable

@Serializable
data class ButtonJson(
    val name:String,
    val rect:Rect? = null,
    val round:Round? = null,
    val roundedRect:RoundedRect? = null,
    val key:List<UByte>,
    val style: ButtonStyleJson? = null,
    val stylePressed:ButtonStyleJson? = null,
    val outlineWidth:Float = 0f
){
    fun toButton(style: ButtonStyleJson, stylePressed: ButtonStyleJson,offset:Point, scale:Float):Button{
        var shapeCount = 0
        rect?.let { shapeCount++ }
        round?.let { shapeCount++ }
        roundedRect?.let { shapeCount++ }
        if(shapeCount != 1) errorBox("每个按钮需要选择 rect round roundedRect 三种形状中的一种")
        val shape = rect ?: roundedRect ?: round ?: nullPtrError()
        return Button(
            name,
            key,
            shape.offset(offset).rescaled(scale),
            (this.style?.setDefault(style) ?: style).toButtonStyle(false,scale),
            (this.stylePressed?.setDefault(stylePressed) ?: stylePressed).toButtonStyle(true,scale)
        )
    }
}