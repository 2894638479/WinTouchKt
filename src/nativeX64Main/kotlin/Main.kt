import container.Container
import dsl.*
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
    val hwndLayered = container.drawScope.hwnd
    hwndLayered.showAndUpdate()


    TopWindow("window",800,800){
        Button(Modifier().width(50).height(50).padding(left = 50, top = 50),Alignment().bottom().right(),"tefs"){
            info("clickkkked!")
        }
        Box(Modifier().width(400).height(400),Alignment()){
            Button(Modifier().width(50).height(50),Alignment().left().middleY(),"s"){
                info("fsdajk")
            }
            Edit(Modifier().width(300).height(100),Alignment().right().middleY(),"initial"){
                info(it)
            }
        }
        Text(Modifier().size(100,50),Alignment().middleX().bottom(),"aaaaaa")
    }
    loopWindowMessage()
}
