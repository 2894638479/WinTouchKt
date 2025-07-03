package container

import button.MutableRect
import button.Rect
import error.errorBox

abstract class NodeWithChild<C:Node>: Node() {
    protected abstract val children:MutableList<C>
    fun addChild(child:C) {
        child.parent = this
        children.add(child)
    }
    fun removeChild(child: C){
        child.parent = null
        if(!children.remove(child)) errorBox("remove child error")
    }
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