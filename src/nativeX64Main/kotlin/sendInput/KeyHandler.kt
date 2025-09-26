package sendInput

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.toKStringFromUtf16
import platform.windows.*

class KeyHandler(val onHideKeyUp:()->Unit,val onExitKeyUp:()->Unit) {
    companion object {
        val KEYEVENT_UP = KEYEVENTF_KEYUP.toUInt()
        const val KEYEVENT_DOWN = 0u

        typealias KeyRange = Iterable<UByte>
        private class UByteRange(val first:UByte, val last:UByte): Iterable<UByte>{
            init {
                require(first <= last)
            }
            override fun iterator() = object :Iterator<UByte>{
                var next = first.toUShort()
                override fun hasNext() = next <= last
                override fun next(): UByte {
                    val result = next
                    next = (next+1u).toUShort()
                    return result.toUByte()
                }
            }
        }
        val customKeys: KeyRange = listOf(0u,255u)
        val directionKeys: KeyRange = UByteRange(0x21u,0x28u)
        val numberKeys: KeyRange = UByteRange(0x30u,0x39u)
        val smallNumberKeys: KeyRange = UByteRange(0x60u,0x69u)
        val symbolKeys: KeyRange = UByteRange(0x6Au,0x6Fu) + UByteRange(0xBAu,0xBFu) + 0xC0u + UByteRange(0xDBu,0xDEu) + 0xE2u
        val characterKeys: KeyRange = UByteRange(0x41u,0x5Au)
        val fKeys: KeyRange = UByteRange(0x70u,0x87u)
        val browserKeys: KeyRange = UByteRange(0xA6u,0xACu)
        val musicKeys: KeyRange = UByteRange(0xADu,0xB3u)
        val mouseKeys: KeyRange = listOf(1u,2u,4u,5u,6u)
        val functionKeys: KeyRange = listOf<UByte>(0x08u,0x09u) +
                UByteRange(0x10u,0x14u) + listOf(0x1Bu,0x2Cu,0x2Du,0x2Eu,0x5Bu,0x5Cu,0x5Du,0x5Fu) +
                UByteRange(0xA0u,0xA5u)
        val otherKeys: KeyRange = listOf<UByte>(0x03u,0x0Cu,0x0Du) +
                UByteRange(0x15u,0x1Au) + UByteRange(0x1Cu,0x1Fu) +
                listOf(0x29u,0x2Au,0x2Bu,0x2Fu,0x90u,0x91u,0xB6u,0xB7u) + UByteRange(0xBAu,0xC0u) + UByteRange(0xDBu,0xFEu)

        val keyCategory = mapOf(
            customKeys to "特殊按键(软件内)",
            directionKeys to "方向键",
            numberKeys to "数字键",
            smallNumberKeys to "小键盘数字键",
            symbolKeys to "符号键",
            characterKeys to "字母键",
            fKeys to "F键",
            browserKeys to "浏览器键",
            musicKeys to "阴影键",
            mouseKeys to "鼠标键",
            functionKeys to "功能键",
            otherKeys to "其它"
        )


        @OptIn(ExperimentalForeignApi::class)
        private val buffer = nativeHeap.allocArray<UShortVar>(256)

        @OptIn(ExperimentalForeignApi::class)
        fun keyNames(code: UByte):String{
            if(code == Keys.HIDE_SHOW.code) return "显示/隐藏"
            if(code == Keys.MENU.code) return "菜单"
            val scanCode = MapVirtualKeyW(code.toUInt(),MAPVK_VK_TO_VSC.toUInt())
            GetKeyNameTextW(scanCode.toInt() shl 16,buffer,256)
            return buffer.toKStringFromUtf16()
        }
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
            Keys.LBUTTON.code -> sendInput {
                type = INPUT_MOUSE.toUInt()
                mi.dwFlags = when(keyEvent) {
                    KEYEVENT_DOWN -> MOUSEEVENTF_LEFTDOWN.toUInt()
                    KEYEVENT_UP -> MOUSEEVENTF_LEFTUP.toUInt()
                    else -> return false
                }
            }
            Keys.RBUTTON.code -> sendInput {
                type = INPUT_MOUSE.toUInt()
                mi.dwFlags = when(keyEvent) {
                    KEYEVENT_DOWN -> MOUSEEVENTF_RIGHTDOWN.toUInt()
                    KEYEVENT_UP -> MOUSEEVENTF_RIGHTUP.toUInt()
                    else -> return false
                }
            }
            Keys.MBUTTON.code -> sendInput {
                type = INPUT_MOUSE.toUInt()
                mi.dwFlags = when(keyEvent) {
                    KEYEVENT_DOWN -> MOUSEEVENTF_MIDDLEDOWN.toUInt()
                    KEYEVENT_UP -> MOUSEEVENTF_MIDDLEUP.toUInt()
                    else -> return false
                }
            }
            Keys.HIDE_SHOW.code ->  if (keyEvent == KEYEVENT_UP) onHideKeyUp()
            Keys.EXIT.code -> if(keyEvent == KEYEVENT_UP) onExitKeyUp()
            else -> return false
        }
        return true
    }
}