package node

import dsl.MutStateList


abstract class NodeWithChild<C: Node>: Node(), MutStateList.Listener<C>{
    override fun onRemove(element: C) {
        element.parent = null
    }
    override fun onAdd(element: C) {
        element.parent = this@NodeWithChild
    }
    override fun onAnyChange(list: List<C>) {
        context?.drawScope?.run { reDraw = true }
    }
    val children = MutStateList<C>().also{ it.listen(false, this) }
}