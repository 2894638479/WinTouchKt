package draw

import button.Button
import button.Rect
import kotlinx.cinterop.*
import platform.windows.*


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