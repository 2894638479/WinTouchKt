package dsl

import platform.windows.SS_CENTER
import platform.windows.SS_LEFT
import platform.windows.SS_RIGHT

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
val A get() = Alignment()

fun Alignment.left() = Alignment(value or 0b1)
fun Alignment.top() = Alignment(value or 0b10)
fun Alignment.right() = Alignment(value or 0b100)
fun Alignment.bottom() = Alignment(value or 0b1000)
fun Alignment.middleX() = Alignment(value or 0b10000)
fun Alignment.middleY() = Alignment(value or 0b100000)