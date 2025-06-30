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

private val json = Json {
    prettyPrint = true
    explicitNulls = false
}

fun String.toContainer():Container{
    val c: Container
    try {
        c = json.decodeFromString<Container>(this)
    } catch (e:Exception) {
        jsonDecodeError(e)
    }
    return c
}