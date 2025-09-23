package node

import dsl.mutStateOf
import geometry.plus
import group.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import node.Container.Status.*
import touch.TouchReceiver
import wrapper.SerializerWrapper

@Serializable(with = Group.GroupSerializer::class)
class Group(
    createDispatcher:(Group)-> GroupTouchDispatcher
): NodeWithChild<Button>() {
    var normalModeTouchDispatcher by mutStateOf(createDispatcher(this))
    var touchDispatcher: GroupTouchDispatcher? = null
    init {
        combine {
            val parent = (parent as? Container) ?: return@combine null
            val group = this@Group
            when(parent.status) {
                NORMAL -> normalModeTouchDispatcher
                DRAG_BUTTON -> object : GroupTouchDispatcher(group) {
                    override fun down(event: TouchReceiver.TouchEvent):Boolean {
                        return event.touched?.let {
                            pointers[event.id] = mutableListOf(it)
                        } != null
                    }
                    override fun move(event: TouchReceiver.TouchEvent): Boolean {
                        val button = pointers[event.id]?.getOrNull(0) ?: return false
                        button.offset += event.point / button.displayScale
                        return true
                    }
                    override fun up(event: TouchReceiver.TouchEvent) = pointers.remove(event.id) != null
                }
                DRAG_GROUP -> object : GroupTouchDispatcher(group) {
                    override fun down(event: TouchReceiver.TouchEvent) = event.touched != null
                    override fun move(event: TouchReceiver.TouchEvent): Boolean {
                        offset += event.point / displayScale
                        return true
                    }
                }
                DRAG_CONTAINER -> null
                SELECT_BUTTON -> object : GroupTouchDispatcher(group) {
                    override fun down(event: TouchReceiver.TouchEvent) = event.touched?.let { pointers[event.id] = mutableListOf(it) } != null
                    override fun move(event: TouchReceiver.TouchEvent) = event.id in pointers
                    override fun up(event: TouchReceiver.TouchEvent) = pointers.remove(event.id)?.let { parent.selected = it[0] } != null
                }
                SELECT_GROUP -> object : GroupTouchDispatcher(group) {
                    override fun down(event: TouchReceiver.TouchEvent) = event.touched != null
                    override fun up(event: TouchReceiver.TouchEvent) = true.also { parent.selected = group }
                }
            }
        }.listen(true) {
            touchDispatcher?.destroy()
            touchDispatcher = it
        }
    }

    val buttons get() = children

    override fun onAnyChange(list: List<Button>) {
        touchDispatcher?.notifyButtonsChanged()
        super.onAnyChange(list)
    }

    object GroupSerializer : SerializerWrapper<Group, GroupSerializer.Descriptor>("Group", Descriptor) {
        object Descriptor: Node.Descriptor<Group>() {
            val type = "type" from { when(normalModeTouchDispatcher::class){
                NormalGroup::class -> 0
                SlideGroup::class ->  1
                HoldSlideGroup::class -> 2
                HoldGroup::class -> 3
                HoldGroupDoubleClk::class -> 4
                TouchPadGroup::class -> 7
                MouseGroup::class -> 8
                ScrollGroup::class -> 9
                else -> error("unknown group type")
            }.toUByte() }
            val buttons = "buttons" from {buttons.list}
            val sensitivity = "sensitivity" from {
                (touchDispatcher as? MovePointGroup)?.sensitivity ?:
                (touchDispatcher as? TouchPadGroup)?.sensitivity
            }
            val slideCount = "slideCount" from {(touchDispatcher as? SlideGroup)?.slideCount}
            val ms = "ms" from {
                (touchDispatcher as? HoldGroupDoubleClk)?.ms ?:
                (touchDispatcher as? TouchPadGroup)?.ms
            }
            val holdIndex = "holdIndex" from {(touchDispatcher as? HoldSlideGroup)?.holdIndex}
        }

        override fun Descriptor.generate(): Group {
            return Group {
                when(type.nullable?.toInt()){
                    0 -> NormalGroup(it)
                    1 -> SlideGroup(it, slideCount.nullable ?: error("no slideCount"))
                    2 -> HoldSlideGroup(it, holdIndex.nonNull)
                    3 -> HoldGroup(it)
                    4 -> HoldGroupDoubleClk(it, ms.nonNull)
                    7 -> TouchPadGroup(it, sensitivity.nonNull, ms.nonNull)
                    8 -> MouseGroup(it, sensitivity.nonNull)
                    9 -> ScrollGroup(it, sensitivity.nonNull)
                    else -> error("unknown group type $type")
                }
            }.also {
                it.addNodeInfo()
                it.buttons += buttons.nonNull
            }
        }
    }

    override fun toString() = "Group${Json.encodeToString(this)}"
}