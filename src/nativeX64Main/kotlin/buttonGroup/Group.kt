package buttonGroup

import button.Button
import button.Button.ButtonSerializer
import button.ButtonStyle
import button.Point
import container.Container
import container.NodeWithChild
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import touch.TouchReceiver
import wrapper.SerializerWrapper
import wrapper.WeakRefDel

@Serializable(with = Group.GroupSerializer::class)
class Group(
    createDispatcher:(Group)-> GroupTouchDispatcher
): NodeWithChild<Button>() {
    val buttons: MutableList<Button> = mutableListOf()
    var touchDispatcher = createDispatcher(this)
        private set
    val editModeTouchDispatcher = object :GroupTouchDispatcher(this) {
        override fun down(event: TouchReceiver.TouchEvent):Boolean {
            return firstOrNull(event.x,event.y)?.let {
                pointers[event.id] = mutableListOf(it)
            } != null
        }
        override fun move(event: TouchReceiver.TouchEvent): Boolean {
            val scope = container.drawScope
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

    override var parent by WeakRefDel<Container>()
    inline val container get() = parent ?: error("group parent is null")
    override val children get() = buttons
    fun addButton(button: Button){
        button.parent = this
        buttons += button
        touchDispatcher.notifyButtonsChanged()
    }
    fun removeButton(button: Button){
        if(!buttons.remove(button)) error("remove button error")
        button.parent = null
        touchDispatcher.notifyButtonsChanged()
    }

    object GroupSerializer : SerializerWrapper<Group, GroupSerializer.Descriptor>("Group",Descriptor) {
        object Descriptor: SerializerWrapper.Descriptor<Group>() {
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
            val offset = "offset" from { offset }
            val buttons = "buttons" from {buttons}
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
            val style = "style" from {style}
            val stylePressed = "stylePressed" from {stylePressed}
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
                it.offset = offset.nullable
                it.style = style.nullable
                it.stylePressed = stylePressed.nullable
                buttons.nonNull.forEach(it::addButton)
            }
        }
    }
}