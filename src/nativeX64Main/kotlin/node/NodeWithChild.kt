package node

import geometry.Rect

abstract class NodeWithChild<C: Node>: Node() {
    protected abstract val children:List<C>
    final override fun calOuterRect(): Rect? {
        var res: Rect.Mutable? = null
        children.forEach {
            it.cache.outerRect?.let {
                res?.plusAssign(it) ?: run { res = it.toMutable() }
            }
        }
        return res?.toRect()
    }

    final override fun iterateChildren(block: (Node) -> Unit) {
        super.iterateChildren(block)
        children.forEach { it.iterateChildren(block) }
    }
}