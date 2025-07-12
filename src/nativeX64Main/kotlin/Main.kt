import container.Container
import error.catchInKotlin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import libs.Clib.GBKToUTF8
import libs.Clib.PrepareForUIAccess
import libs.Clib.freeStr
import logger.info
import window.loopWindowMessage
import window.registerGui
import window.registerLayered
import window.showWindow
import wrapper.GuiWindow
import wrapper.cutBottom
import wrapper.cutTop
import wrapper.padding


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

fun Main(args: Array<String>) = processArgs(args).apply {
    registerLayered()
    registerGui()
    val container = json.json.decodeFromString<Container>(jsonStr)
    val hwnd = container.drawScope.hwnd
    showWindow(hwnd)
    val a = object : GuiWindow("windo",800,600) {
        val button1 = button("b1啊啊啊") {
            info("clicked")
        }
        val window1 = object :GuiWindow("subwindow",parent = this){
            val button1 = button("fafdsfds"){
                info("clicked1")
            }
            override fun onSize() {
                button1.moveRect(relativeRect.apply { padding(20) })
            }
        }
        override fun onSize() {
            button1.moveRect(relativeRect.apply { padding(100);cutTop(0.7f) })
            window1.moveRect(relativeRect.apply { padding(100);cutBottom(0.3f) })
        }
    }.apply { show() }
    loopWindowMessage()
}
