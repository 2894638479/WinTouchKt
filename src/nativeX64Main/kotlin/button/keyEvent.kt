package button

import platform.windows.KEYEVENTF_KEYUP
import platform.windows.MAPVK_VK_TO_VSC
import platform.windows.MapVirtualKeyA
import platform.windows.keybd_event


val KEYEVENT_UP = KEYEVENTF_KEYUP.toUInt()
val KEYEVENT_DOWN = 0u

fun sendKeyEvent(key:UByte,keyEvent:UInt){
    val vsc = MapVirtualKeyA(key.toUInt(), MAPVK_VK_TO_VSC.toUInt()).toUByte()
    keybd_event(key,vsc,keyEvent, 0u)
    println("$key   $keyEvent")
}
fun sendAllKeyEvent(list:List<UByte>,keyEvent: UInt) {
    for (key in list) sendKeyEvent(key, keyEvent)
}
inline fun sendAllKeyEventFilter(list:List<UByte>, keyEvent: UInt,filter:(UByte)->Boolean) {
    for (key in list.filter(filter)) sendKeyEvent(key, keyEvent)
}