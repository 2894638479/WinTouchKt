package json

import container.Container
import error.jsonDecodeError
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

fun String.toContainer():Container{
    val cj:ContainerJson
    try {
        cj = Json.decodeFromString<ContainerJson>(this)
    } catch (e:Exception) {
        jsonDecodeError(e)
    }
    return cj.toContainer()
}