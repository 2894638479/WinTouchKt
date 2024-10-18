package file

import container.Container
import error.*
import kotlinx.cinterop.*
import kotlinx.serialization.json.Json
import platform.posix.*

val defaultDataPath = "data.json"

@OptIn(ExperimentalForeignApi::class)
fun readFile(filePath: String,bufferSize:Int = 1024) = memScoped {
    val file = fopen(filePath, "r") ?: if(access(filePath, F_OK) == 0){
        fileOpenError(filePath)
    } else {
        fileNotExists(filePath)
    }
    val buffer = allocArray<ByteVar>(bufferSize)
    val stringBuilder = StringBuilder()
    while (true) {
        val bytesRead = fread(buffer, 1u, bufferSize.toULong(), file).toInt()
        val content = ByteArray(bytesRead).apply {
            memcpy(refTo(0), buffer, bytesRead.toULong())
        }
        val str = content.decodeToString()
        stringBuilder.append(str)
        if(bytesRead < bufferSize) break
    }
    fclose(file)
    stringBuilder.toString()
}

@OptIn(ExperimentalForeignApi::class)
fun writeFile(filename: String, content: String) = memScoped {
    val file = fopen(filename, "w") ?: fileOpenError(filename)
    fprintf(file, content)
    fclose(file)
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
        return Json.decodeFromString(str)
    } catch (e:Exception) {
        jsonDecodeError(fileName,e)
    }
}