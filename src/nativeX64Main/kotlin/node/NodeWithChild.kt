package node

import geometry.MutableRect
import geometry.Rect

abstract class NodeWithChild<C: Node>: Node() {
    protected abstract val children:List<C>
    final override fun calOuterRect(): Rect? {
        var res: MutableRect? = null
        children.forEach {
            it.cache.outerRect?.let {
                res?.plusAssign(it) ?: run { res = it.toMutableRect() }
            }
        }
        return res?.toRect()
    }

    final override fun iterateChildren(block: (Node) -> Unit) {
        super.iterateChildren(block)
        children.forEach { it.iterateChildren(block) }
    }
}