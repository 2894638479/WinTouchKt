package json

import button.Point
import buttonGroup.*
import error.groupTypeError
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable

@Serializable
data class GroupJson(
    val offset: Point,
    val buttons: List<ButtonJson> = emptyList(),
    val type: UByte = 0u,
    val sensitivity:Float = 1f,
    val slideCount:UInt = 1u,
    val ms:ULong = 300uL,
    val holdIndex:Int = 0,
    val style:ButtonStyleJson? = null,
    val stylePressed: ButtonStyleJson? = null,
) {
    @OptIn(ExperimentalForeignApi::class)
    fun toGroup(style: ButtonStyleJson, stylePressed: ButtonStyleJson): Group {
        val style = this.style?.setDefault(style) ?: style
        val stylePressed = this.stylePressed?.setDefault(stylePressed) ?: stylePressed
        val btns = buttons.map { it.toButton(style,stylePressed) }
        val group: Group = when(type.toInt()){
            0 -> NormalGroup(btns, offset)
            1 -> SlideGroup(btns,offset,slideCount)
            2 -> HoldSlideGroup(btns,offset,holdIndex)
            3 -> HoldGroup(btns,offset)
            4 -> HoldGroupDoubleClk(btns,offset,ms)
            8 -> MouseGroup(btns,offset,sensitivity)
            9 -> ScrollGroup(btns,offset,sensitivity)
            else -> groupTypeError(type)
        }
        return group
    }
}