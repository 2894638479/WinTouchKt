package dsl

import wrapper.*

class BoxScope(modifier: Modifier, alignment: Alignment, parent: GuiWindow?, name:String = "box"):
    GuiScope(parent,name,modifier, alignment) {
    override fun onSize() {
        visibleChildren.forEach {
            if(!it.hwnd.visible) return@forEach
            val modifier = it.modifier
            val align = it.alignment
            val rect = hwnd.rect.apply { toOrigin() }
            allocRECT {
                if (modifier.width == 0) {
                    left = rect.left
                    right = rect.right
                } else {
                    val width = modifier.run { width + paddingW }
                    if (align.middleX) {
                        left = rect.midX - width / 2
                        right = left + width
                    } else if (align.right) {
                        right = rect.right
                        left = rect.right - width
                    } else {
                        left = rect.left
                        right = left + width
                    }
                }
                left += modifier.paddingLeft
                right -= modifier.paddingRight

                if (modifier.height == 0) {
                    top = rect.top
                    bottom = rect.bottom
                } else {
                    val height = modifier.run { height + paddingH }
                    if (align.middleY) {
                        top = rect.midY - height / 2
                        bottom = top + height
                    } else if (align.bottom) {
                        bottom = rect.bottom
                        top = bottom - height
                    } else {
                        top = rect.top
                        bottom = top + height
                    }
                }
                top += modifier.paddingTop
                bottom -= modifier.paddingBottom

                it.hwnd.rect = this
            }
        }
    }
}