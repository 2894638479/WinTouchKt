import dsl.*
import error.catchInKotlin
import error.wrapExceptionName
import gui.MainContent
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import libs.Clib.GBKToUTF8
import libs.Clib.PrepareForUIAccess
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

    val scope = MutState.SimpleScope()
    class A{
        var m1 by mutStateOf(0)
        var m2 by mutStateOf("test")
    }
    val a by mutStateOf(A())
    val b by mutStateOf(4)

    val combined1 = scope.combine {
        "${a.m2} $b"
    }
    val combined2 = scope.combine {
        "${a.m1} ${a.m2}"
    }
    val combined3 = scope.combine {
        "${a.m1} ${a.m2} $b"
    }
    scope.run {
        combined1.listen { warning("combined1 $it") }
        combined2.listen { warning("combined2 $it") }
        combined3.listen { warning("combined3 $it") }
    }


    TopWindow("window",800,600){
        wrapExceptionName("creating MainContent") {
            MainContent(container)
        }
    }
    loopWindowMessage()
}
