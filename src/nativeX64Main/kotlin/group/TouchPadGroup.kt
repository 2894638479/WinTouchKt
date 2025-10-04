package group

import dsl.mutStateOf
import node.Group
import geometry.Point
import logger.info
import platform.windows.GetTickCount64
import sendInput.moveCursor
import touch.GroupTouchDispatcher
import touch.GroupTouchReceiver
import touch.TouchReceiver
import window.dispatch

class TouchPadGroup(sensitivity:Float? = null, ms:ULong? = null): GroupTouchDispatcher() {
    var sensitivity by mutStateOf(sensitivity ?: 1f)
    var ms by mutStateOf(ms ?: 200uL)
    override fun create(group: Group) = object : GroupTouchReceiver(group){
        private var lastTouchPoint : Point? = null
        private var lastDownTime = buttons.map { 0uL }.toULongArray()
        private var keyDownCount = buttons.map { 0u }.toUIntArray()

        override fun notifyButtonsChanged() {
            lastDownTime = buttons.map { 0uL }.toULongArray()
            keyDownCount = buttons.map { 0u }.toUIntArray()
            super.notifyButtonsChanged()
        }

        override fun move(event: TouchReceiver.TouchEvent): Boolean {
            if(pointers[event.id] == null) return true
            moveCursor((event.point - (lastTouchPoint ?: return false)) * sensitivity)
            lastTouchPoint = Point(event.x, event.y)
            return true
        }

        override fun down(event: TouchReceiver.TouchEvent): Boolean {
            return event.touched?.let{
                pointers[event.id] = mutableListOf(it)
                lastTouchPoint = Point(event.x,event.y)
                val index = buttons.indexOf(it)
                val time = GetTickCount64()
                if(time - lastDownTime[index] < ms){
                    it.down()
                    keyDownCount[index]++
                } else {
                    it.down { false }
                }
                lastDownTime[index] = time
            } != null
        }

        override fun up(event: TouchReceiver.TouchEvent): Boolean {
            pointers[event.id]?.get(0)?.let {
                val index = buttons.indexOf(it)
                val time = GetTickCount64()
                if(keyDownCount[index] > 0u){
                    it.up()
                    keyDownCount[index]--
                    return@let
                }
                it.up { false }
                if(time - lastDownTime[index] < ms){
                    dispatch(ms.toUInt()) {
                        if(keyDownCount[index] == 0u) {
                            it.triggerKeys()
                        }
                    }
                }
            } ?: return false
            pointers.remove(event.id)
            return true
        }
    }
}