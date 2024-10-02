package sendInput

import button.Button
import drawScope
import mainContainer
import platform.posix.exit
import platform.windows.MOUSEEVENTF_LEFTDOWN
import platform.windows.MOUSEEVENTF_LEFTUP
import platform.windows.MOUSEEVENTF_RIGHTDOWN
import platform.windows.MOUSEEVENTF_RIGHTUP

const val KEY_HIDE:UByte = 0u
const val KEY_LBUTTON:UByte = 1u
const val KEY_RBUTTON:UByte = 2u
const val KEY_EXIT:UByte = 254u



fun Button.processSpecialKey(key:UByte, keyEvent:UInt):Boolean {
    when(key){
        KEY_LBUTTON -> sendInput {
            mi.dwFlags = when(keyEvent) {
                KEYEVENT_DOWN -> MOUSEEVENTF_LEFTDOWN.toUInt()
                KEYEVENT_UP -> MOUSEEVENTF_LEFTUP.toUInt()
                else -> return false
            }
        }
        KEY_RBUTTON -> sendInput {
            mi.dwFlags = when(keyEvent) {
                KEYEVENT_DOWN -> MOUSEEVENTF_RIGHTDOWN.toUInt()
                KEYEVENT_UP -> MOUSEEVENTF_RIGHTUP.toUInt()
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