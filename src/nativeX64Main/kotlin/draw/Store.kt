package draw

import error.nullPtrError
import wrapper.*
import kotlin.collections.HashMap
import kotlin.collections.set

object Store {
    private val brushes = HashMap<Color, D2dBrush>(100)
    private val fonts = HashMap<Font, D2dFont>(100)
    var writeFactory: D2dWriteFactory? = null
    var target: D2dTarget? = null
    fun font(key:Font) = fonts[key] ?: (writeFactory ?: nullPtrError()).let { D2dFont.create(it,key) }.apply { fonts[key] = this }
    fun brush(key:Color) = brushes[key] ?: (target ?: nullPtrError()).let { D2dBrush.create(it,key) }.apply { brushes[key] = this }
}