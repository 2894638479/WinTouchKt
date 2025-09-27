package group

import group.GroupType.Value.Companion.value
import touch.GroupTouchDispatcher
import kotlin.reflect.KClass

enum class GroupType(val value:Value<*>) {
    NORMAL(value(0u,"普通分组","按下手指时按下按键，松开手指时松开按键。", ::NormalGroup)),
    SLIDE(value(1u,"滑动分组","按下后，滑动手指到组内另一按钮上，会松开当前按钮并按下新的按钮。如果要按下的按钮和要松开的按钮包含同一键值，这些键值不会被松开再重新按下。把同时拖动个数调大后，可以用来达到单个手指按下多个按钮的效果。", ::SlideGroup)),
    HOLD_SLIDE(value(2u,"摇杆滑动分组","组内的第一个按钮为中心，可以把中心设为潜行键，四周设为移动键。", ::HoldSlideGroup)),
    HOLD(value(3u,"保持分组","按下时切换按钮的状态", ::HoldGroup)),
    HOLD_DOUBLE_CLICK(value(4u,"双击保持分组","双击切换按钮的状态", ::HoldDoubleClickGroup)),
    TOUCHPAD(value(7u,"触摸板分组","滑动手指时会移动鼠标指针，单击会点击按键，双击会双击按键。", ::TouchPadGroup)),
    MOUSE(value(8u,"鼠标分组","滑动手指会移动鼠标指针。", ::MouseGroup)),
    SCROLL(value(9u,"滚轮分组","滑动鼠标会滚动鼠标滚轮，支持横向和竖向。", ::ScrollGroup));
    val groupName get() = value.groupName
    val code get() = value.code
    val description get() = value.description
    val default get() = value.default
    val clazz get() = value.clazz

    class Value <T: GroupTouchDispatcher>(val code: UByte, val groupName: String, val description: String, val default:()->T, val clazz: KClass<T>){
        companion object {
            inline fun <reified T: GroupTouchDispatcher> value(code:UByte,name: String, description: String, noinline default:()->T) =
                Value(code,name,description,default,T::class)
        }
    }
    companion object {
        private val map = GroupType.entries.associate { it.value.clazz to it }
        val <T: GroupTouchDispatcher> KClass<T>.type get() = (map[this]) ?: error("unknown GroupType $this")
        val <T: GroupTouchDispatcher> T.type get() = this::class.type
        fun UByte.toGroupType() = entries.firstOrNull { it.code == this } ?: error("finding code $this in GroupType")
    }
}