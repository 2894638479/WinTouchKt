import dsl.*
import error.catchInKotlin
import error.wrapExceptionName
import gui.MainContent
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import libs.Clib.GBKToUTF8
import libs.Clib.freeStr
import logger.warning
import node.Container
import window.loopWindowMessage
import window.registerGui
import window.registerLayered


@OptIn(ExperimentalForeignApi::class)
fun main() = catchInKotlin {
//    PrepareForUIAccess()
    val argc = platform.posix.__argc
    val argv = platform.posix.__argv
    Main(Array(argc - 1){ i ->
        val gbk = argv?.get(i + 1) ?: error("argv is null")
        val utf8 = GBKToUTF8(gbk) ?: error("utf8 convert error")
        val str =  utf8.toKString()
        freeStr(utf8)
        str
    }).run { Unit }
}

fun Main(args: Array<String>) = processArgs(args).apply {
    registerLayered()
    registerGui()
    val container = wrapExceptionName("json decode failed"){
        json.decodeFromString<Container>(jsonStr)
    }
    warning(container.toString())
    val hwndLayered = container.drawScope.hwnd
    hwndLayered.showAndUpdate()

    TopWindow("window", M.minWidth(800).minHeight(600)) {
        wrapExceptionName("creating MainContent") {
            MainContent(container)
        }
    }
    loopWindowMessage()
}
