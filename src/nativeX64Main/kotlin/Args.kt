import json.readFile


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