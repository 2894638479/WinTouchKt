package touch

import node.Group

abstract class GroupTouchDispatcher {
    abstract fun create(group:Group): GroupTouchReceiver
    context(group:Group)
    fun create() = create(group)
}