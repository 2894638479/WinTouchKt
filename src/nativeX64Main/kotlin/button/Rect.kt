package button

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import platform.windows.RECT
import kotlin.math.max
import kotlin.math.min

@Serializable
data class Rect(
    @SerialName("l") var left:Int,
    @SerialName("t") var top:Int,
    @SerialName("r") var right:Int,
    @SerialName("b") var bottom:Int,
){
    constructor(rect:RECT) : this(rect.left,rect.top,rect.right,rect.bottom)
    val width get() = right - left
    val height get() = bottom - top

    fun RECT.copyThis() = apply {
        left = this@Rect.left
        top = this@Rect.top
        right = this@Rect.right
        bottom = this@Rect.bottom
    }
    @OptIn(ExperimentalForeignApi::class)
    inline fun withRECT(block: RECT.()->Unit) = memScoped {
        alloc<RECT> {
            copyThis()
            block()
        }
    }
    operator fun plus(other:Rect):Rect{
        return Rect(
            min(left,other.left),
            min(top,other.top),
            max(right,other.right),
            max(bottom,other.bottom)
        )
    }
    operator fun plusAssign(other:Rect){
        left = min(left,other.left)
        top = min(top,other.top)
        right = max(right,other.right)
        bottom = max(bottom,other.bottom)
    }
    operator fun plusAssign(point:Point){
        left += point.x
        top += point.y
        right += point.x
        bottom += point.y
    }
}
inline val RECT.width get() = right - left
inline val RECT.height get() = bottom - top
fun RECT.equal(other:RECT):Boolean {
    return left == other.left
            && right == other.right
            && top == other.top
            && bottom == other.bottom
}
fun RECT.copyFrom(other:RECT){
    left = other.left
    right = other.right
    top = other.top
    bottom = other.bottom
}