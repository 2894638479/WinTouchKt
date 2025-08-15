package node

import dsl.MutStateList
import geometry.Rect
import kotlin.math.max
import kotlin.math.min


abstract class NodeWithChild<C: Node>: Node() {
    val children = MutStateList<C>().apply {
        listen(false,object :MutStateList.Listener<C>{
            override fun onRemove(element: C) {
                element.parent = null
            }
            override fun onAdd(element: C) {
                element.parent = this@NodeWithChild
            }
            override fun onAnyChange() {}
        })
    }
}