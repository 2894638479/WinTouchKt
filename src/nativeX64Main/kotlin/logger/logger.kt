package logger

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.wcstr
import platform.windows.MB_ICONERROR
import platform.windows.MB_ICONINFORMATION
import platform.windows.MB_OK
import platform.windows.MessageBox

var printInfo = true
var printWarning = true
var printError = true

fun info(content:String){
    if (printInfo) println("[info] $content")
}
fun warning(content:String){
    if (printWarning) println("[warning] $content")
}
fun printError(content:String){
    if (printError) println("[error] $content")
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