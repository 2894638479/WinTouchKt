package dsl

import platform.windows.SS_CENTER
import platform.windows.SS_LEFT
import platform.windows.SS_RIGHT
import wrapper.*

class Modifier internal constructor(
    internal var width:Int,
    internal var height:Int,
    internal var weight:Float,
    internal var paddingTop:Int,
    internal var paddingLeft:Int,
    internal var paddingBottom:Int,
    internal var paddingRight:Int,
){
    constructor():this(0,0,1f,0,0,0,0)
    internal val fullWidth get() = width + paddingLeft + paddingRight
    internal val fullHeight get() = height + paddingTop + paddingBottom
}
fun Modifier.size(width: Int,height: Int) = apply { this.width = width; this.height = height }
fun Modifier.width(width: Int) = apply { this.width = width }
fun Modifier.height(height: Int) = apply { this.height = height }
fun Modifier.padding(value:Int) = apply {
    paddingLeft = value
    paddingTop = value
    paddingRight = value
    paddingBottom = value
}
fun Modifier.padding(horizontal:Int = 0,vertical:Int = 0) = apply {
    paddingRight = horizontal
    paddingLeft = horizontal
    paddingBottom = vertical
    paddingTop = vertical
}
fun Modifier.padding(left:Int = 0,top:Int = 0,right:Int = 0,bottom:Int = 0) = apply {
    paddingLeft = left
    paddingTop = top
    paddingRight = right
    paddingBottom = bottom
}

value class Alignment internal constructor(internal val value:Int){
    constructor():this(0)
    internal val left get() = value and 0b1 != 0
    internal val top get() = value and 0b10 != 0
    internal val right get() = value and 0b100 != 0
    internal val bottom get() = value and 0b1000 != 0
    internal val middleX get() = value and 0b10000 != 0
    internal val middleY get() = value and 0b100000 != 0
    val staticStyle get() = if (right) SS_RIGHT else if(middleX) SS_CENTER else SS_LEFT
}
fun Alignment.left() = Alignment(value or 0b1)
fun Alignment.top() = Alignment(value or 0b10)
fun Alignment.right() = Alignment(value or 0b100)
fun Alignment.bottom() = Alignment(value or 0b1000)
fun Alignment.middleX() = Alignment(value or 0b10000)
fun Alignment.middleY() = Alignment(value or 0b100000)

@DslMarker
annotation class ComponentScope

abstract class GuiComponent(val modifier:Modifier,val alignment:Alignment){
    abstract val hwnd:Hwnd
}

class GuiHwnd(modifier: Modifier, alignment: Alignment, override val hwnd: Hwnd):GuiComponent(modifier, alignment)

fun TopWindow(name: String,minW:Int,minH:Int,block: BoxScope.() -> Unit){
    BoxScope(Modifier().size(minW,minH),Alignment(),null,name).apply {
        block()
        hwnd.show()
        hwnd.update()
        onSize()
    }
}


@ComponentScope
abstract class GuiScope(
    parent:GuiWindow?,name:String,
    modifier: Modifier,
    alignment: Alignment
): GuiComponent(modifier, alignment) {
    private val window = object : GuiWindow(name,
        if(parent == null) modifier.width else 0,
        if(parent == null) modifier.height else 0,parent){
        override fun onSize() = this@GuiScope.onSize()
    }
    override val hwnd get() = window.hwnd
    protected val children = mutableListOf<GuiComponent>()
    abstract fun onSize()
    fun Box(modifier: Modifier,alignment: Alignment,block:BoxScope.()->Unit){
        children += BoxScope(modifier,alignment,window).apply(block)
    }
    fun Button(modifier: Modifier,alignment: Alignment,text:String,onClick:()->Unit){
        children += GuiHwnd(modifier, alignment,window.button(text,onClick))
    }
    fun Edit(modifier:Modifier, alignment: Alignment, text: String, onEdit:(String)->Unit){
        children += GuiHwnd(modifier, alignment,window.edit(text,onEdit))
    }
    fun Custom(modifier: Modifier,alignment: Alignment,block:()->GuiWindow){
        children += GuiHwnd(modifier, alignment, block().hwnd)
    }
    fun Text(modifier:Modifier,alignment: Alignment,text: String){
        children += GuiHwnd(modifier, alignment, window.text(text,alignment))
    }
}


class BoxScope(modifier: Modifier, alignment: Alignment, parent:GuiWindow?,name:String = "box"):
    GuiScope(parent,name,modifier, alignment) {
    override fun onSize() {
        children.forEach {
            val modifier = it.modifier
            val align = it.alignment
            val rect = hwnd.rect
            rect.toOrigin()
            allocRECT {
                if(modifier.width == 0){
                    left = rect.left
                    right = rect.right
                } else {
                    if(align.middleX){
                        left = rect.midX - modifier.fullWidth / 2
                        right = left + modifier.fullWidth
                    } else if(align.right){
                        right = rect.right
                        left = rect.right - modifier.fullWidth
                    } else {
                        left = rect.left
                        right = left + modifier.fullWidth
                    }
                }
                left += modifier.paddingLeft
                right -= modifier.paddingRight

                if(modifier.height == 0){
                    top = rect.top
                    bottom = rect.bottom
                } else {
                    if(align.middleY){
                        top = rect.midY - modifier.fullHeight / 2
                        bottom = top + modifier.fullHeight
                    } else if(align.bottom){
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