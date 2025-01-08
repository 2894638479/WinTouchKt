package error

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.wcstr
import platform.windows.MB_ICONERROR
import platform.windows.MB_OK
import platform.windows.MessageBox

@OptIn(ExperimentalForeignApi::class)
fun errorBox(error: String, e:Exception? = null):Nothing = memScoped {
    var str = error
    if(e != null) str += ("\n\n" + e.message)
    MessageBox!!(null, str.wcstr.ptr, "WinTouchKt: error".wcstr.ptr, (MB_OK or MB_ICONERROR).toUInt())
    error(error)
}

fun errorBox(errorCn:String,errorEn:String,e:Exception? = null): Nothing = errorBox(errorCn + "\n\n" + errorEn,e)

fun emptyGroupError():Nothing = errorBox(
    "配置中含有空的group",
    "reading an empty group"
)

fun logicError(info:String):Nothing = errorBox(
    "程序中发生逻辑错误" + "\n\n" +
    "logic error happened" + "\n\n" +
    "info: $info"
)

fun emptyContainerError():Nothing = errorBox(
    "配置是空的",
    "reading an empty container"
)

fun fileOpenError(fileName:String):Nothing = errorBox(
    "无法打开文件：$fileName",
    "cannot open file: $fileName"
)

fun fileNotExists(fileName: String):Nothing = if(
    Regex("^[1-9a-zA-Z\\s/.,;()~'\"-]*$").matches(fileName)
) {
    errorBox(
        "文件不存在：$fileName",
        "file not exists: $fileName"
    )
} else {
    errorBox(
        "文件不存在，或路径中含有非英文字符：$fileName",
        "file not exists: $fileName"
    )
}

fun jsonDecodeError(fileName: String,e:Exception? = null):Nothing = if(fileName.endsWith(".json")) {
    errorBox(
        "json解析错误，可能是格式不正确或变量名不对：$fileName",
        "json decode error: $fileName",
        e
    )
} else {
    errorBox(
        "json解析错误，可能文件不是.json格式：$fileName",
        "json decode error: $fileName",
        e
    )
}

fun notPlaceJsonError():Nothing = errorBox(
    "检测到data.json不存在，请在当前目录放置data.json或把配置文件拖到exe上",
    "please place data.json at current path or drag file onto exe"
)

fun argumentManyError():Nothing = errorBox(
    "参数过多，只能输入1个参数",
    "too many arguments"
)

fun groupTypeError(value:UByte):Nothing = errorBox(
    "未知的group类型：$value",
    "unknown group type: $value"
)

fun slideCountError(value: UInt):Nothing = errorBox(
    "slideCount错误：$value",
    "slide count error: $value"
)

fun nullPtrError():Nothing = errorBox(
    "空指针异常，可能是程序逻辑出错",
    "null pointer exception"
)

fun holdIndexError(value:Int):Nothing = errorBox(
    "holdIndex错误：$value，请确认此值>=0，<按钮总个数",
    "holdIndex Error"
)
