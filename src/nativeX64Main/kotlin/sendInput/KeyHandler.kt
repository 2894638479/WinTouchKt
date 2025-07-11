package sendInput

import platform.windows.*

class KeyHandler(val onHideKeyUp:()->Unit,val onExitKeyUp:()->Unit) {
    companion object {
        val KEYEVENT_UP = KEYEVENTF_KEYUP.toUInt()
        const val KEYEVENT_DOWN = 0u


        const val KEY_HIDE:UByte = 0u
        val KEY_LBUTTON:UByte = VK_LBUTTON.toUByte()
        val KEY_RBUTTON:UByte = VK_RBUTTON.toUByte()
        val KEY_MBUTTON:UByte = VK_MBUTTON.toUByte()
        const val KEY_EXIT:UByte = 255u
    }
    fun down(key:UByte) = sendKeyEvent(key, KEYEVENT_DOWN)
    fun up(key:UByte) = sendKeyEvent(key, KEYEVENT_UP)

    fun downAll(key: Collection<UByte>) = key.forEach(::down)
    fun upAll(key: Collection<UByte>) = key.forEach(::up)

    fun sendKeyEvent(key: UByte, keyEvent: UInt){
        val vsc = MapVirtualKeyA(key.toUInt(), MAPVK_VK_TO_VSC.toUInt()).toUByte()
        if(processSpecialKey(key, keyEvent)) return
        keybd_event(key,vsc,keyEvent, 0u)
    }

    private fun processSpecialKey(key:UByte, keyEvent:UInt):Boolean {
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
            KEY_HIDE ->  if (keyEvent == KEYEVENT_UP) onHideKeyUp()
            KEY_EXIT -> if(keyEvent == KEYEVENT_UP) onExitKeyUp()
            else -> return false
        }
        return true
    }
}