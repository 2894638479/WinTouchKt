package draw

import button.Button
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.wcstr
import platform.windows.HDC
import platform.windows.Rectangle
import platform.windows.SelectObject
import platform.windows.TextOut


@OptIn(ExperimentalForeignApi::class)
fun DrawScope.drawButtons(
    hdc:HDC?,
    buttons: Collection<Button>,
){
    buttons.forEach {
        SelectObject(hdc,if (it.pressed) colorBrushBright else colorBrushDark)
        val rect = it.rect
        Rectangle(hdc, rect.left, rect.top, rect.right, rect.bottom)
    }

    SelectObject(hdc,textBrush)
    buttons.forEach{
        memScoped {
            val rect = it.rect
            val text = it.name
            TextOut!!(hdc, rect.left, rect.top, text.wcstr.ptr, text.length)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
fun DrawScope.drawButton(
    hdc:HDC?,
    button: Button,
){
    val rect = button.rect
    val text = button.name

    SelectObject(hdc,if (button.pressed) colorBrushBright else colorBrushDark)
    Rectangle(hdc, rect.left, rect.top, rect.right, rect.bottom)

    SelectObject(hdc,textBrush)
    memScoped {
        TextOut!!(hdc, rect.left, rect.top, text.wcstr.ptr, text.length)
    }
}