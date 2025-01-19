import container.Container
import draw.DrawScope
import draw.Store
import error.*
import json.readFile
import json.toContainer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import libs.Clib.GBKToUTF8
import libs.Clib.PrepareForUIAccess
import libs.Clib.freeStr
import platform.posix.exit
import platform.windows.Sleep

private var drawScopeRaw: DrawScope? = null
private var mainContainerRaw: Container? = null
val drawScope:DrawScope get() = drawScopeRaw!!
val drawScopeNullable:DrawScope? get() = drawScopeRaw
val mainContainer get() = mainContainerRaw!!

@OptIn(ExperimentalForeignApi::class)
fun main(){
//    PrepareForUIAccess()
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
    val result = processArgs(args)
    result.sleepTime.let { if(it > 0u) Sleep(it) }
    window { hwnd ->
        drawScopeRaw = DrawScope(hwnd)
        drawScope.initStore()
        mainContainerRaw = result.jsonStr.toContainer()
        drawScope.iterateButtons = mainContainer::forEachButton
        mainContainer.invalidate = drawScope::invalidate
        drawScope.alpha = mainContainer.alpha
    }
}

class ArgParseResult(
    val jsonStr:String,
    val sleepTime:UInt
)

fun processArgs(args:Array<String>):ArgParseResult{
    var jsonCont:String? = null
    var argTask:((String)->Unit)? = null
    var sleepTime = 0u
    args.forEachIndexed { index, arg ->
        argTask?.let {
            it(arg)
            argTask = null
            return@forEachIndexed
        }
        if(arg.startsWith('-')){
            when(arg){
                "-s" -> argTask = {
                    sleepTime = it.toUIntOrNull() ?: unknownOptError("-s",it)
                }
                "-h" -> {
                    argumentUsageInfo()
                    exit(0)
                }
                "-d" -> argTask = {
                    jsonCont = it
                }
                else -> unknownArgError(arg)
            }
        } else {
            if(index == 0) {
                jsonCont = readFile(arg) ?: fileOpenError(arg)
            } else {
                unknownArgError(arg)
            }
        }
    }
    return ArgParseResult(jsonCont ?: noProfileError(),sleepTime)
}