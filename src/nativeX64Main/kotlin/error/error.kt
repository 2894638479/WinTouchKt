package error

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.wcstr
import platform.windows.MB_OK
import platform.windows.MessageBox

@OptIn(ExperimentalForeignApi::class)
fun errorBox(error: String):Nothing = memScoped {
    MessageBox!!(null,error.wcstr.ptr,"WinTouchKt: error".wcstr.ptr, MB_OK.toUInt())
    error(error)
}

fun errorBox(errorCn:String,errorEn:String): Nothing = errorBox(errorCn + "\n\n" + errorEn)

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
    "无法打开文件，或文件不存在：$fileName",
    "cannot open file: $fileName"
)

fun jsonDecodeError(fileName: String):Nothing = if(fileName.endsWith(".json")) {
    errorBox(
        "json解析错误，可能是格式不正确或变量名不对：$fileName",
        "json decode error: $fileName"
    )
} else {
    errorBox(
        "json解析错误，可能文件不是.json格式：$fileName",
        "json decode error: $fileName"
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

fun groupTypeError(value:Int):Nothing = errorBox(
    "未知的group类型：$value",
    "unknown group type: $value"
)
