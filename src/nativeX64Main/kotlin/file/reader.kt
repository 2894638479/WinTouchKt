package file

import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
fun readFile(filePath: String,bufferSize:Int = 1024) = memScoped {
    val file = fopen(filePath, "r") ?: error("Failed to open file: $filePath")
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
    val file = fopen(filename, "w")
    if (file != null) {
        fprintf(file, content)
        fclose(file)
    }
}