package json

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