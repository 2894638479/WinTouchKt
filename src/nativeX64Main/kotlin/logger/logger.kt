package logger

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.wcstr
import logger.AnsiColor.RED
import logger.AnsiColor.WHITE
import logger.AnsiColor.YELLOW
import logger.AnsiColor.color
import platform.windows.*

var printInfo = true
    get() = field && !disablePrint
var printWarning = true
    get() = field && !disablePrint
var printError = true
    get() = field && !disablePrint


var disablePrint = false


object AnsiColor {
    const val RESET = "\u001B[0m"
    const val BLACK = "\u001B[30m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val PURPLE = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"

    fun String.color(value:String) = value + this + RESET
}

fun info(content:Any){
    if (printInfo) println("[info] $content".color(WHITE))
}
fun warning(content:Any){
    if (printWarning) println("[warning] $content".color(YELLOW))
}
fun printError(content:Any){
    if (printError) println("[error] $content".color(RED))
}


@OptIn(ExperimentalForeignApi::class)
fun errorBox(content:String){
    memScoped {
        val p1 = content.wcstr.ptr
        val p2 =  "WinTouchKt".wcstr.ptr
        val res = MessageBox!!(null, p1,p2, (MB_OK or MB_ICONERROR).toUInt())
        info("shown error box(return value $res): $content")
    }
}

@OptIn(ExperimentalForeignApi::class)
fun infoBox(info:String) = memScoped {
    val res = MessageBox!!(null, info.wcstr.ptr, "WinTouchKt: info".wcstr.ptr, (MB_OK or MB_ICONINFORMATION).toUInt())
    info("shown info box(return value $res): $info")
}

@OptIn(ExperimentalForeignApi::class)
fun warningBox(warning:String) = memScoped {
    val res = MessageBox!!(null, warning.wcstr.ptr, "WinTouchKt: info".wcstr.ptr, (MB_OK or MB_ICONWARNING).toUInt())
    info("shown warning box(return value $res): $warning")
}