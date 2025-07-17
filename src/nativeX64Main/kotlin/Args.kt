import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.toKString
import kotlinx.serialization.json.Json
import libs.Clib.freeStr


@OptIn(ExperimentalForeignApi::class)
fun readFile(filePath: String):String? {
    val cstr = libs.Clib.readFile(filePath.cstr)
    val str = cstr?.toKString()
    freeStr(cstr)
    return str
}

val json = Json {
    prettyPrint = true
    explicitNulls = false
}


class ArgParseResult(
    val jsonStr:String,
)

fun processArgs(args:Array<String>):ArgParseResult{
    if (args.size == 1) {
        val path = args[0]
        val json = readFile(path) ?: error("failed open file $path")
        return ArgParseResult(json)
    }
    if(args.isEmpty()) error("no args provided")
    error("too many args")
}