package node

import dsl.MutStateList


abstract class NodeWithChild<C: Node>: Node() {
    val children = MutStateList<C>().apply {
        listen(false,object :MutStateList.Listener<C>{
            override fun onRemove(element: C) {
                element.parent = null
            }
            override fun onAdd(element: C) {
                element.parent = this@NodeWithChild
            }
            override fun onAnyChange() {
                context?.drawScope?.run { reDraw = true }
            }
        })
    }
}