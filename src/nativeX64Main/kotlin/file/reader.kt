package file

import container.Container
import error.argumentManyError
import error.fileOpenError
import error.jsonDecodeError
import error.notPlaceJsonError
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.toKString
import kotlinx.serialization.json.Json
import libs.Clib.freeStr

val defaultDataPath = "data.json"

@OptIn(ExperimentalForeignApi::class)
fun readFile(filePath: String):String {
    val cstr = libs.Clib.readFile(filePath.cstr)
    val str = cstr?.toKString() ?: fileOpenError(filePath)
    freeStr(cstr)
    return str
}

fun readContainer(args:Array<String>):Container{
    val fileName = when(args.size){
        0 -> defaultDataPath
        1 -> args[0]
        else -> argumentManyError()
    }
    val str:String
    try {
        str = readFile(fileName)
    } catch (e:Exception) {
        if(args.isEmpty()) notPlaceJsonError()
        else throw e
    }
    try {
        return Json.decodeFromString<ContainerJson>(str).toContainer()
    } catch (e:Exception) {
        jsonDecodeError(fileName,e)
    }
}