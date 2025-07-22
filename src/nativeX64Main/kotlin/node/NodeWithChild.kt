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
            override fun onAnyChange() {
                _outerRect = null
            }
        })
    }

    override fun calOuterRect(): Rect {
        var rect:Rect.Mutable? = null
        children.forEach {
            val outerRect = it.outerRect
            rect?.run {
                left = min(left,outerRect.left)
                right = max(right,outerRect.right)
                top = min(top,outerRect.top)
                bottom = max(bottom,outerRect.bottom)
            } ?: run { rect = outerRect.toMutable() }
        }
        return rect?.toRect() ?: Rect.empty
    }
}