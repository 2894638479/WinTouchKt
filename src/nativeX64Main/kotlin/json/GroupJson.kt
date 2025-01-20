package json

import button.Point
import buttonGroup.*
import error.groupTypeError
import error.infoBox
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
    fun toGroup(style: ButtonStyleJson, stylePressed: ButtonStyleJson, scale:Float): Group {
        val style = this.style?.setDefault(style) ?: style
        val stylePressed = this.stylePressed?.setDefault(stylePressed) ?: stylePressed
        val btns = buttons.map { it.toButton(style,stylePressed,offset,scale) }
        val group: Group = when(type.toInt()){
            0 -> NormalGroup(btns)
            1 -> SlideGroup(btns,slideCount)
            2 -> HoldSlideGroup(btns,holdIndex)
            3 -> HoldGroup(btns)
            4 -> HoldGroupDoubleClk(btns,ms)
            8 -> MouseGroup(btns,sensitivity)
            9 -> ScrollGroup(btns,sensitivity)
            else -> groupTypeError(type)
        }
        return group
    }
}