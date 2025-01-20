package json

import button.*
import buttonGroup.*
import container.Container
import draw.Color
import error.errorBox
import error.groupTypeError
import error.nullPtrError
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.math.roundToInt


fun ColorJson.toColor():Color{
    fun Float.getUByte():UByte{
        return roundToInt().run {
            if(this < 0 || this >= 256) errorBox("颜色值必须为0~255之间")
            else toUByte()
        }
    }
    return Color(
        r.getUByte(),
        g.getUByte(),
        b.getUByte()
    )
}

fun PointJson.toPoint() = Point(x,y)
fun RectJson.toRect() = Rect(left, top, right, bottom, outlineWidth)
fun RoundJson.toRound() = Round(x, y, r, outlineWidth)
fun RoundedRectJson.toRoundedRect() = RoundedRect(left, top, right, bottom, r, outlineWidth)

@OptIn(ExperimentalForeignApi::class)
fun ContainerJson.toContainer(): Container {
    val style = style?.setDefault(style) ?: ButtonStyleJson()
    val stylePressed = stylePressed?.setDefault(stylePressed) ?: ButtonStyleJson()
    return Container(
        groups.map { it.toGroup(style, stylePressed,scale) },
        alpha
    )
}


fun ButtonStyleJson.toButtonStyle(pressed:Boolean, scale:Float) = ButtonStyle(
    pressed,
    scale,
    color?.toColor(),
    textColor?.toColor(),
    outlineColor?.toColor(),
    fontFamily,
    fontSize,
    fontStyle,
    fontWeight
)


fun ButtonJson.toButton(style: ButtonStyleJson, stylePressed: ButtonStyleJson, offset: Point, scale:Float): Button {
    var shapeCount = 0
    rect?.let { shapeCount++ }
    round?.let { shapeCount++ }
    roundedRect?.let { shapeCount++ }
    if(shapeCount != 1) errorBox("每个按钮需要选择 rect round roundedRect 三种形状中的一种")
    val shape = rect?.toRect()
        ?: roundedRect?.toRoundedRect()
        ?: round?.toRound()
        ?: nullPtrError()
    return Button(
        name,
        key,
        shape.offset(offset).rescaled(scale),
        (this.style?.setDefault(style) ?: style).toButtonStyle(false,scale),
        (this.stylePressed?.setDefault(stylePressed) ?: stylePressed).toButtonStyle(true,scale)
    )
}


@OptIn(ExperimentalForeignApi::class)
fun GroupJson.toGroup(style: ButtonStyleJson, stylePressed: ButtonStyleJson, scale:Float): Group {
    val style = this.style?.setDefault(style) ?: style
    val stylePressed = this.stylePressed?.setDefault(stylePressed) ?: stylePressed
    val btns = buttons.map { it.toButton(style,stylePressed,offset.toPoint(),scale) }
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