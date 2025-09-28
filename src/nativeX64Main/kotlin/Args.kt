import error.wrapExceptionName
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.toKString
import kotlinx.serialization.json.Json
import libs.Clib.freeStr
import node.Container


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
    ignoreUnknownKeys = true
}

fun createContainerFromFilePath(path: String): Container{
    val jsonStr = readFile(path) ?: error("cannot open file $path")
    val container = wrapExceptionName("json decode error"){
        json.decodeFromString<Container>(jsonStr)
    }
    container.drawScope.hwnd.showAndUpdate()
    return container
}