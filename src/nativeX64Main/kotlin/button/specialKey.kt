package button

import drawScope
import hook.unHook
import hook.hooked
import hook.setHook
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import mainContainer
import platform.posix.exit
import platform.windows.GetModuleHandle

const val KEY_HIDE:UByte = 0u
const val KEY_EXIT:UByte = 254u
const val KEY_DISABLE_MOUSE:UByte = 255u

@OptIn(ExperimentalForeignApi::class)
fun processSpecialKey(button:Button) {
    val keys = button.key
    if(button.pressed) return
    if(keys.contains(KEY_HIDE)){
        if(drawScope.showStatus){
            drawScope.hideButtons(button)
        } else {
            drawScope.showButtons { mainContainer.forEachButton(it) }
        }
    } else if (keys.contains(KEY_DISABLE_MOUSE)) {
        if(hooked){
            unHook()
        } else {
            val hInstance = GetModuleHandle!!(null)
            setHook(hInstance)
        }
    } else if (keys.contains(KEY_EXIT)) {
        exit(0)
    }
}