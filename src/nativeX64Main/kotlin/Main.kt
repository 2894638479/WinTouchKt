import error.catchInKotlin
import gui.window.openChooseFileWindow
import gui.window.openProtectWindow
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import libs.Clib.GBKToUTF8
import libs.Clib.PrepareForUIAccess
import libs.Clib.freeStr
import node.Container
import window.loopWindowMessage
import window.registerGui
import window.registerLayered


const val VERSION = "4.0"

@OptIn(ExperimentalForeignApi::class)
fun main() = catchInKotlin {
    PrepareForUIAccess()
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

fun Main(args: Array<String>) {
    registerLayered()
    registerGui()

    if(args.getOrNull(0) != null) {
        val container = createContainerFromFilePath(args[0])
        openProtectWindow(container)
    } else {
        openChooseFileWindow({
            val container = Container().apply { filePath = it }
            openProtectWindow(container)
        }) {
            val container = createContainerFromFilePath(it)
            openProtectWindow(container)
        }
    }

    loopWindowMessage()
}
