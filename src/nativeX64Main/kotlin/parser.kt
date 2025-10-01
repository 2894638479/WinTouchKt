import error.wrapExceptionName
import kotlinx.cinterop.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import libs.Clib.freeStr
import node.Container
import platform.windows.GetOpenFileNameW
import platform.windows.GetSaveFileNameW
import platform.windows.MAX_PATH
import platform.windows.OFN_FILEMUSTEXIST
import platform.windows.OFN_OVERWRITEPROMPT
import platform.windows.OFN_PATHMUSTEXIST
import platform.windows.OPENFILENAMEW
import platform.windows.WCHARVar
import wrapper.Hwnd


@OptIn(ExperimentalForeignApi::class)
fun readFile(filePath: String):String? {
    val cstr = libs.Clib.readFile(filePath.cstr)
    val str = cstr?.toKString()
    freeStr(cstr)
    return str
}

@OptIn(ExperimentalForeignApi::class)
fun writeFile(filePath: String, content: String): Boolean {
    val code = libs.Clib.writeFile(filePath.cstr,content.cstr)
    return code == 0
}



val json = Json {
    prettyPrint = true
    explicitNulls = false
    ignoreUnknownKeys = true
}

inline fun <reified T> Json.copy(instance:T) = decodeFromString<T>(encodeToString(instance))

fun createContainerFromFilePath(path: String): Container{
    val jsonStr = readFile(path) ?: error("cannot open file $path")
    val container = wrapExceptionName("json decode error"){
        json.decodeFromString<Container>(jsonStr)
    }
    container.filePath = path
    container.drawScope.hwnd.showAndUpdate()
    return container
}


@OptIn(ExperimentalForeignApi::class)
fun MemScope.makeFilter(vararg filter: String): CPointer<WCHARVar>{
    val totalLength = filter.sumOf { it.length + 1 } + 1
    val buffer = allocArray<WCHARVar>(totalLength)
    var offset = 0
    for (s in filter) {
        for (c in s) {
            buffer[offset++] = c.code.toUShort()
        }
        buffer[offset++] = 0u
    }
    buffer[offset] = 0u
    return buffer
}

@OptIn(ExperimentalForeignApi::class)
fun chooseFile(parent: Hwnd) = memScoped {
    val ofn = alloc<OPENFILENAMEW> {
        lStructSize = sizeOf<OPENFILENAMEW>().toUInt()
        hwndOwner = parent.HWND
        lpstrFile = allocArray<UShortVar>(MAX_PATH)
        nMaxFile = MAX_PATH.toUInt()
        lpstrFilter = makeFilter("json","*.json")
        nFilterIndex = 1u
        lpstrTitle = "选择文件".wcstr.ptr
        Flags = (OFN_PATHMUSTEXIST or OFN_FILEMUSTEXIST).toUInt()
        lpstrDefExt = "json".wcstr.ptr
    }
    if (GetOpenFileNameW(ofn.ptr) != 0) ofn.lpstrFile?.toKStringFromUtf16() else null
}


@OptIn(ExperimentalForeignApi::class)
fun chooseSaveFile(parent:Hwnd) = memScoped {
    val ofn = alloc<OPENFILENAMEW> {
        lStructSize = sizeOf<OPENFILENAMEW>().toUInt()
        hwndOwner = parent.HWND
        lpstrFile = allocArray<UShortVar>(MAX_PATH)
        nMaxFile = MAX_PATH.toUInt()
        lpstrFilter = makeFilter("json","*.json")
        nFilterIndex = 1u
        lpstrTitle = "保存到文件".wcstr.ptr
        Flags = (OFN_PATHMUSTEXIST or OFN_OVERWRITEPROMPT).toUInt()
        lpstrDefExt = "json".wcstr.ptr
    }
    if (GetSaveFileNameW(ofn.ptr) != 0) ofn.lpstrFile?.toKStringFromUtf16() else null
}