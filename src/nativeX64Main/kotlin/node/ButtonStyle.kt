package node

import dsl.mutStateNull
import geometry.Color
import geometry.Font
import kotlinx.serialization.Serializable
import wrapper.SerializerWrapper


@Serializable(with = ButtonStyle.Serializer::class)
class ButtonStyle {
    var color by mutStateNull<Color>()
    var textColor by mutStateNull<Color>()
    var outlineColor by mutStateNull<Color>()
    var fontFamily by mutStateNull<String>()
    var fontSize by mutStateNull<Float>()
    var fontStyle by mutStateNull<Font.Style>()
    var fontWeight by mutStateNull<Int>()

    object Serializer :SerializerWrapper<ButtonStyle,Serializer.Descriptor>("ButtonStyle",Descriptor){
        object Descriptor:SerializerWrapper.Descriptor<ButtonStyle>(){
            val color = "color" from {color}
            val textColor = "textColor" from {textColor}
            val outlineColor = "outlineColor" from {outlineColor}
            val fontFamily = "fontFamily" from {fontFamily}
            val fontSize = "fontSize" from {fontSize}
            val fontStyle = "fontStyle" from {fontStyle?.string}
            val fontWeight = "fontWeight" from {fontWeight}
        }
        override fun Descriptor.generate(): ButtonStyle {
            return ButtonStyle().also {
                it.color = color.nullable
                it.textColor = textColor.nullable
                it.outlineColor = outlineColor.nullable
                it.fontFamily = fontFamily.nullable
                it.fontSize = fontSize.nullable
                it.fontStyle = fontStyle.nullable?.let { Font.Style.byString(it) }
                it.fontWeight = fontWeight.nullable
            }
        }
    }
}