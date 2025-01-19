package error

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.wcstr
import platform.windows.*

@OptIn(ExperimentalForeignApi::class)
fun infoBox(infoCn:String,infoEn:String) = memScoped {
    MessageBox!!(null, (infoCn + "\n\n" + infoEn).wcstr.ptr, "WinTouchKt: info".wcstr.ptr, (MB_OK or MB_ICONINFORMATION).toUInt())
}
@OptIn(ExperimentalForeignApi::class)
fun infoBox(info:String) = memScoped {
    MessageBox!!(null, (info).wcstr.ptr, "WinTouchKt: info".wcstr.ptr, (MB_OK or MB_ICONINFORMATION).toUInt())
}

@OptIn(ExperimentalForeignApi::class)
fun errorBox(error: String, e:Exception? = null,beforeExit:()->Unit = {}):Nothing = memScoped {
    var str = error
    if(e != null) str += ("\n\n" + e.message)
    MessageBox!!(null, str.wcstr.ptr, "WinTouchKt: error".wcstr.ptr, (MB_OK or MB_ICONERROR).toUInt())
    beforeExit()
    error(error)
}

fun errorBox(errorCn:String,errorEn:String,e:Exception? = null,beforeExit:()->Unit = {}): Nothing
    = errorBox(errorCn + "\n\n" + errorEn,e,beforeExit)

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

fun fileNotExists(fileName: String):Nothing = errorBox(
    "文件不存在：$fileName",
    "file not exists: $fileName"
)

fun jsonDecodeError(e:Exception? = null):Nothing = errorBox(
    "json解析错误，可能是格式不正确或变量名不对",
    "json decode error",
    e
)


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

fun entryParaError():Nothing = errorBox(
    "入口点参数错误",
    "entry parament error"
)

fun fontWeightError(min:UShort,max:UShort,cur:Int):Nothing = errorBox(
    "字体粗细错误：必须位于 $min~$max 之间，当前为$cur",
    "font weight error: must between $min~$max, current:$cur"
)

fun fontStyleError(cur:String,list:Set<String>):Nothing = errorBox(
    "未知字体样式：$cur，可选：$list",
    "unknown font style: $cur, available: $list"
)

fun fontCreateError():Nothing = errorBox(
    "字体创建失败",
    "font create error"
)

fun brushCreateError():Nothing = errorBox(
    "笔刷创建失败",
    "brush create error"
)

fun direct2dInitializeError():Nothing = errorBox(
    "direct2d初始化失败",
    "direct2d initialize error"
)

fun argumentUsageInfo() = infoBox(
    "用法：\n" +
            "\tWinTouchKt <配置文件路径>\n" +
            "\tWinTouchKt -d <配置文件内容>\n" +
            "参数：\n" +
            "\t-s <延迟启动时间（毫秒）>\n" +
            "\t-d <json内容>\n" +
            "\t-h 显示帮助"
    ,
    "usage:\n" +
            "\tWinTouchKt <json path>\n" +
            "\tWinTouchKt -d <json string content>\n" +
            "options:\n" +
            "\t-s <delay time(ms)>\n" +
            "\t-d <json content>\n" +
            "\t-h show help window"
)

fun unknownArgError(arg:String):Nothing = errorBox(
    "未知参数：$arg",
    "unknown argument:$arg"
){ argumentUsageInfo() }

fun unknownOptError(arg:String,opt:String):Nothing = errorBox(
    "未知参数：$arg $opt",
    "unknown argument:$arg $opt"
){ argumentUsageInfo() }

fun noProfileError():Nothing = errorBox(
    "未传入配置文件。请把配置拖到exe上，或者在命令行中一第一个参数传入",
    "no profile provided. please drag file on exe or pass as first argument"
)
