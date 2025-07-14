package dsl

import wrapper.*

class BoxScope(modifier: Modifier, alignment: Alignment, parent: GuiWindow?, name:String = "box"):
    GuiScope(parent,name,modifier, alignment) {
    override fun onSize() {
        children.forEach {
            if(!it.hwnd.visible) return@forEach
            val modifier = it.modifier
            val align = it.alignment
            val rect = hwnd.rect
            rect.toOrigin()
            allocRECT {
                if (modifier.width == 0) {
                    left = rect.left
                    right = rect.right
                } else {
                    if (align.middleX) {
                        left = rect.midX - modifier.fullWidth / 2
                        right = left + modifier.fullWidth
                    } else if (align.right) {
                        right = rect.right
                        left = rect.right - modifier.fullWidth
                    } else {
                        left = rect.left
                        right = left + modifier.fullWidth
                    }
                }
                left += modifier.paddingLeft
                right -= modifier.paddingRight

                if (modifier.height == 0) {
                    top = rect.top
                    bottom = rect.bottom
                } else {
                    if (align.middleY) {
                        top = rect.midY - modifier.fullHeight / 2
                        bottom = top + modifier.fullHeight
                    } else if (align.bottom) {
                        bottom = rect.bottom
                        top = bottom - modifier.fullHeight
                    } else {
                        top = rect.top
                        bottom = top + modifier.fullHeight
                    }
                }
                top += modifier.paddingTop
                bottom -= modifier.paddingBottom

                it.hwnd.setRect(this)
            }
        }
    }
}