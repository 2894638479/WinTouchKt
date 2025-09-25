package node

import dsl.mutStateOf
import error.wrapExceptionName
import geometry.Point
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
    createDispatcher:(Group)-> GroupTouchDispatcher = ::NormalGroup
): NodeWithChild<Button>() {
    var normalDispatcherCreator by mutStateOf(createDispatcher)
    var normalDispatcher = normalDispatcherCreator(this)
        private set
    var touchDispatcher: GroupTouchDispatcher? = null
    init {
        extract { normalDispatcherCreator }.listen {
            normalDispatcher.destroy()
            normalDispatcher = it(this)
        }
        combine {
            touchDispatcher?.destroy()
            touchDispatcher = null
            val parent = (parent as? Container) ?: return@combine // if I return null at here, it will throw kotlin.NoWhenBranchMatchedException at runtime. I have no idea about this, but this code can run now.
            val group = this@Group
            touchDispatcher = when(parent.status) {
                NORMAL -> {
                    normalDispatcher = wrapExceptionName("create dispatcher failed"){
                        normalDispatcherCreator(group)
                    }
                    normalDispatcher
                }
                DRAG_BUTTON -> object : GroupTouchDispatcher(group) {
                    var lastTouchPoint: Point? = null
                    override fun down(event: TouchReceiver.TouchEvent): Boolean {
                        pointers[event.id] = mutableListOf(event.touched ?: return false)
                        lastTouchPoint = event.point
                        return true
                    }
                    override fun move(event: TouchReceiver.TouchEvent): Boolean {
                        val button = pointers[event.id]?.getOrNull(0) ?: return false
                        button.offset += (event.point - (lastTouchPoint ?: error("no lastTouchPoint"))) / displayScale
                        lastTouchPoint = event.point
                        return true
                    }
                    override fun up(event: TouchReceiver.TouchEvent): Boolean {
                        return pointers.remove(event.id) != null
                    }
                }
                DRAG_GROUP -> object : GroupTouchDispatcher(group) {
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
        }
    }

    val buttons by children

    override fun onAnyChange(list: List<Button>) {
        touchDispatcher?.notifyButtonsChanged()
        super.onAnyChange(list)
    }

    object GroupSerializer : SerializerWrapper<Group, GroupSerializer.Descriptor>("Group", Descriptor) {
        object Descriptor: Node.Descriptor<Group>() {
            val type = "type" from { when(normalDispatcher::class){
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
            val buttons = "buttons" from {buttons}
            val sensitivity = "sensitivity" from {
                (normalDispatcher as? MovePointGroup)?.sensitivity ?:
                (normalDispatcher as? TouchPadGroup)?.sensitivity
            }
            val slideCount = "slideCount" from {(normalDispatcher as? SlideGroup)?.slideCount}
            val ms = "ms" from {
                (normalDispatcher as? HoldGroupDoubleClk)?.ms ?:
                (normalDispatcher as? TouchPadGroup)?.ms
            }
        }

        override fun Descriptor.generate(): Group {
            val type = type.nonNull
            val ms = ms.nullable
            val sensitivity = sensitivity.nullable
            val slideCount = slideCount.nullable
            val createDispatcher:(Group)-> GroupTouchDispatcher = when(type.toInt()){
                0 -> { { NormalGroup(it) } }
                1 -> { { SlideGroup(it, slideCount!!) } }
                2 -> { { HoldSlideGroup(it) } }
                3 -> { { HoldGroup(it) } }
                4 -> { { HoldGroupDoubleClk(it, ms!!) } }
                7 -> { { TouchPadGroup(it, sensitivity!!, ms!!) } }
                8 -> { { MouseGroup(it, sensitivity!!) } }
                9 -> { { ScrollGroup(it, sensitivity!!) } }
                else -> error("unknown group type $type")
            }
            return Group(createDispatcher).also {
                it.addNodeInfo()
                it.children += buttons.nonNull
            }
        }
    }

    override fun toString() = "Group${Json.encodeToString(this)}"
}