package sendInput

import button.Button
import platform.windows.KEYEVENTF_KEYUP
import platform.windows.MAPVK_VK_TO_VSC
import platform.windows.MapVirtualKeyA
import platform.windows.keybd_event


val KEYEVENT_UP = KEYEVENTF_KEYUP.toUInt()
val KEYEVENT_DOWN = 0u

inline fun Button.sendAllKeyEventFilter(keyEvent: UInt, filter:(UByte)->Boolean) {
    for (key in key.filter(filter)) sendKeyEvent(key, keyEvent)
}
fun Button.sendAllKeyEvent(keyEvent: UInt) {
    for (key in key) sendKeyEvent(key, keyEvent)
}
fun Button.sendKeyEvent(key:UByte, keyEvent:UInt){
    val vsc = MapVirtualKeyA(key.toUInt(), MAPVK_VK_TO_VSC.toUInt()).toUByte()
    if(processSpecialKey(key, keyEvent)) return
    keybd_event(key,vsc,keyEvent, 0u)
}