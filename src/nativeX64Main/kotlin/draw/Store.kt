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
    fun font(key:Font) = fonts[key] ?: writeFactory?.createFont(key)?.apply { fonts[key] = this } ?: nullPtrError()
    fun brush(key:Color) = brushes[key] ?: target?.createBrush(key)?.apply { brushes[key] = this } ?: nullPtrError()
}