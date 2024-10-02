package sendInput

import button.Button
import drawScope
import mainContainer
import platform.posix.exit
import platform.windows.*

const val KEY_HIDE:UByte = 0u
val KEY_LBUTTON:UByte = VK_LBUTTON.toUByte()
val KEY_RBUTTON:UByte = VK_RBUTTON.toUByte()
val KEY_MBUTTON:UByte = VK_MBUTTON.toUByte()
const val KEY_EXIT:UByte = 254u



fun Button.processSpecialKey(key:UByte, keyEvent:UInt):Boolean {
    when(key){
        KEY_LBUTTON -> sendInput {
            type = INPUT_MOUSE.toUInt()
            mi.dwFlags = when(keyEvent) {
                KEYEVENT_DOWN -> MOUSEEVENTF_LEFTDOWN.toUInt()
                KEYEVENT_UP -> MOUSEEVENTF_LEFTUP.toUInt()
                else -> return false
            }
        }
        KEY_RBUTTON -> sendInput {
            type = INPUT_MOUSE.toUInt()
            mi.dwFlags = when(keyEvent) {
                KEYEVENT_DOWN -> MOUSEEVENTF_RIGHTDOWN.toUInt()
                KEYEVENT_UP -> MOUSEEVENTF_RIGHTUP.toUInt()
                else -> return false
            }
        }
        KEY_MBUTTON -> sendInput {
            type = INPUT_MOUSE.toUInt()
            mi.dwFlags = when(keyEvent) {
                KEYEVENT_DOWN -> MOUSEEVENTF_MIDDLEDOWN.toUInt()
                KEYEVENT_UP -> MOUSEEVENTF_MIDDLEUP.toUInt()
                else -> return false
            }
        }
        KEY_HIDE -> {
            if (keyEvent == KEYEVENT_UP) {
                if (drawScope.showStatus) {
                    drawScope.hideButtons(this)
                } else {
                    drawScope.showButtons { mainContainer.forEachButton(it) }
                }
            }
        }
        KEY_EXIT -> if(keyEvent == KEYEVENT_UP) exit(0)
        else -> return false
    }
    return true
}