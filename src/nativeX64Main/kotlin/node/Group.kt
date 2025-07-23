package node

import geometry.Point
import group.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger.warning
import node.Container.ContainerSerializer.Descriptor.from
import touch.TouchReceiver
import wrapper.SerializerWrapper
import wrapper.WeakRefDel

@Serializable(with = Group.GroupSerializer::class)
class Group(
    createDispatcher:(Group)-> GroupTouchDispatcher
): NodeWithChild<Button>() {
    var touchDispatcher = createDispatcher(this)
        private set
    val editModeTouchDispatcher = object : GroupTouchDispatcher(this) {
        override fun down(event: TouchReceiver.TouchEvent):Boolean {
            return firstOrNull(event.x,event.y)?.let {
                pointers[event.id] = mutableListOf(it)
            } != null
        }
        override fun move(event: TouchReceiver.TouchEvent): Boolean {
            val scope = context?.drawScope ?: error("context is null")
            val button = pointers[event.id]?.getOrNull(0) ?: error("pointer id not down")
            scope.toErase(button)
            val offset = Point(event.x,event.y)
            button.offset = button.offset?.plus(offset) ?: offset
            scope.toDrawAll()
            return true
        }
        override fun up(event: TouchReceiver.TouchEvent): Boolean {
            pointers.remove(event.id) ?: error("pointer id not down")
            return true
        }
    }
    val buttons get() = children

    object GroupSerializer : SerializerWrapper<Group, GroupSerializer.Descriptor>("Group", Descriptor) {
        object Descriptor: Node.Descriptor<Group>() {
            val type = "type" from { when(touchDispatcher){
                is SlideGroup ->  1
                is HoldSlideGroup -> 2
                is HoldGroup -> 3
                is HoldGroupDoubleClk -> 4
                is TouchPadGroup -> 7
                is MouseGroup -> 8
                is ScrollGroup -> 9
                is NormalGroup -> 0
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