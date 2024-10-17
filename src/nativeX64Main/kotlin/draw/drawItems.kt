package draw

import button.Button
import kotlinx.cinterop.*
import platform.windows.*

@OptIn(ExperimentalForeignApi::class)
fun drawButtons(hdc:HDC?, buttons: Collection<Button>){
    buttons.forEach {
        drawButton(hdc,it)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun drawButton(hdc:HDC?, button: Button){
    val rect = button.rect
    val text = button.name

    SelectObject(hdc,button.brush)
    Rectangle(hdc, rect.left, rect.top, rect.right, rect.bottom)

    memScoped {
        SetTextColor(hdc,button.textC.ref)
        SelectObject(hdc,button.font)
        button.rect.withRECT {
            DrawText!!(hdc, text.wcstr.ptr, -1, this.ptr, (DT_CENTER or DT_SINGLELINE or DT_VCENTER).toUInt())
        }
    }
}