import container.Container
import draw.DrawScope
import error.entryParaError
import file.readContainer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import libs.Clib.GBKToUTF8
import libs.Clib.freeStr

private var drawScopeRaw: DrawScope? = null
private var mainContainerRaw: Container? = null
val drawScope:DrawScope get() = drawScopeRaw!!
val drawScopeNullable:DrawScope? get() = drawScopeRaw
val mainContainer get() = mainContainerRaw!!

@OptIn(ExperimentalForeignApi::class)
fun main(){
    val argc = platform.posix.__argc
    val argv = platform.posix.__argv
    Main(Array(argc - 1){ i ->
        val gbk = argv?.get(i + 1) ?: entryParaError()
        val utf8 = GBKToUTF8(gbk) ?: entryParaError()
        val str =  utf8.toKString()
        freeStr(utf8)
        str
    })
}

@OptIn(ExperimentalForeignApi::class)
fun Main(args: Array<String>) {
    for (arg in args) {
        println(arg)
    }
    mainContainerRaw = readContainer(args)
    window { hwnd ->
        drawScopeRaw = DrawScope(hwnd,mainContainer::forEachButton)
        mainContainer.invalidate = drawScope::invalidate
        drawScope.alpha = mainContainer.alpha
        println("initialized")
    }
}