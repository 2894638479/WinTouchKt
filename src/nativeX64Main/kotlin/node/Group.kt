package node

import dsl.mutStateOf
import geometry.Point
import geometry.plus
import group.*
import group.GroupType.Companion.toGroupType
import group.GroupType.Companion.type
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import node.Container.Status.*
import touch.GroupTouchDispatcher
import touch.GroupTouchReceiver
import touch.TouchReceiver
import wrapper.SerializerWrapper

@Serializable(with = Group.GroupSerializer::class)
class Group(dispatcher: GroupTouchDispatcher): NodeWithChild<Button>() {
    var dispatcher by mutStateOf(dispatcher)
    override val defaultName get() = dispatcher.type.groupName
    val touchReceiver by combine {
        val group = this@Group
        val parent = (parent as? Container) ?: return@combine null
        when(parent.status) {
            NORMAL -> {
                group.dispatcher.create(group)
            }
            DRAG_BUTTON -> object : GroupTouchReceiver(group) {
                var lastTouchPoint: Point? = null
                override fun down(event: TouchReceiver.TouchEvent): Boolean {
                    pointers[event.id] = mutableListOf(event.touched ?: return false)
                    lastTouchPoint = event.point
                    return true
                }
                override fun move(event: TouchReceiver.TouchEvent): Boolean {
                    val button = pointers[event.id]?.getOrNull(0) ?: return false
                    button.offset += (event.point - (lastTouchPoint ?: error("no lastTouchPoint"))) / displayScale
                    button.snapTo(buttons)
                    lastTouchPoint = event.point
                    return true
                }
                override fun up(event: TouchReceiver.TouchEvent): Boolean {
                    return pointers.remove(event.id) != null
                }
            }
            DRAG_GROUP -> object : GroupTouchReceiver(group) {
                var lastTouchPoint: Point? = null
                override fun down(event: TouchReceiver.TouchEvent): Boolean {
                    pointers[event.id] = mutableListOf(event.touched ?: return false)
                    lastTouchPoint = event.point
                    return true
                }
                override fun move(event: TouchReceiver.TouchEvent): Boolean {
                    val button = pointers[event.id]?.getOrNull(0) ?: return false
                    offset += (event.point - (lastTouchPoint ?: error("no lastTouchPoint"))) / parent.displayScale
                    lastTouchPoint = event.point
                    return true
                }
                override fun up(event: TouchReceiver.TouchEvent): Boolean {
                    return pointers.remove(event.id) != null
                }
            }
            DRAG_CONTAINER -> null
            SELECT_BUTTON -> object : GroupTouchReceiver(group) {
                override fun down(event: TouchReceiver.TouchEvent) = event.touched?.let { pointers[event.id] = mutableListOf(it) } != null
                override fun move(event: TouchReceiver.TouchEvent) = event.id in pointers
                override fun up(event: TouchReceiver.TouchEvent) = pointers.remove(event.id)?.let { parent.selected = it[0] } != null
            }
            SELECT_GROUP -> object : GroupTouchReceiver(group) {
                override fun down(event: TouchReceiver.TouchEvent) = event.touched != null
                override fun up(event: TouchReceiver.TouchEvent) = true.also { parent.selected = group }
            }
        }
    }

    val buttons by children

    override fun onAnyChange(list: List<Button>) {
        touchReceiver?.notifyButtonsChanged()
        super.onAnyChange(list)
    }

    object GroupSerializer : SerializerWrapper<Group, GroupSerializer.Descriptor>("Group", Descriptor) {
        object Descriptor: Node.Descriptor<Group>() {
            val type = "type" from { dispatcher.type.code }
            val buttons = "buttons" from {buttons}
            val sensitivity = "sensitivity" from {
                (dispatcher as? MouseGroup)?.sensitivity ?:
                (dispatcher as? ScrollGroup)?.sensitivity ?:
                (dispatcher as? TouchPadGroup)?.sensitivity
            }
            val slideCount = "slideCount" from {(dispatcher as? SlideGroup)?.slideCount}
            val ms = "ms" from {
                (dispatcher as? HoldDoubleClickGroup)?.ms ?:
                (dispatcher as? TouchPadGroup)?.ms
            }
        }

        override fun Descriptor.generate(): Group {
            return Group(when(type.nonNull.toGroupType()){
                GroupType.NORMAL -> NormalGroup()
                GroupType.SLIDE -> SlideGroup(slideCount.nullable)
                GroupType.HOLD_SLIDE -> HoldSlideGroup()
                GroupType.HOLD -> HoldGroup()
                GroupType.HOLD_DOUBLE_CLICK -> HoldDoubleClickGroup(ms.nullable)
                GroupType.TOUCHPAD -> TouchPadGroup(sensitivity.nullable, ms.nullable)
                GroupType.MOUSE -> MouseGroup(sensitivity.nullable)
                GroupType.SCROLL -> ScrollGroup(sensitivity.nullable)
            }).also {
                it.addNodeInfo()
                it.children += buttons.nonNull
            }
        }
    }

    override fun toString() = "Group${Json.encodeToString(this)}"
}