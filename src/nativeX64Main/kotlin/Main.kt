import dsl.*
import error.catchInKotlin
import error.wrapExceptionName
import gui.MainContent
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.ShortVar
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.wcstr
import libs.Clib.GBKToUTF8
import libs.Clib.freeStr
import logger.info
import logger.warning
import node.Container
import platform.posix.exit
import platform.posix.wchar_t
import platform.posix.wchar_tVar
import platform.windows.GetOpenFileNameA
import platform.windows.GetOpenFileNameW
import platform.windows.MAX_PATH
import platform.windows.OFN_FILEMUSTEXIST
import platform.windows.OFN_PATHMUSTEXIST
import platform.windows.OPENFILENAMEA
import platform.windows.OPENFILENAMEW
import window.loopWindowMessage
import window.registerGui
import window.registerLayered
import wrapper.Hwnd
import wrapper.WindowProcess
import wrapper.alloc


@OptIn(ExperimentalForeignApi::class)
fun main() = catchInKotlin {
//    PrepareForUIAccess()
    val argc = platform.posix.__argc
    val argv = platform.posix.__argv
    Main(Array(argc - 1){ i ->
        val gbk = argv?.get(i + 1) ?: error("argv is null")
        val utf8 = GBKToUTF8(gbk) ?: error("utf8 convert error")
        val str =  utf8.toKString()
        freeStr(utf8)
        str
    }).run { Unit }
}

fun openMainWindow(container: Container) = TopWindow("配置主界面", M.minSize(800,600)) {
    wrapExceptionName("creating MainContent") {
        MainContent(container)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun chooseFile(parent: Hwnd) = memScoped {
    val ofn = alloc<OPENFILENAMEW>()
    val buffer = allocArray<UShortVar>(MAX_PATH)

//    val s = "json\u0000*.json\u0000"
//    val wcs: CPointer<wchar_tVar> = allocArray(s.length)
//    for (i in s.indices) {
//        wcs[i] = s[i].code.convert()
//    }

    ofn.lStructSize = sizeOf<OPENFILENAMEW>().toUInt()
    ofn.hwndOwner = parent.HWND
    ofn.lpstrFile = buffer
    ofn.nMaxFile = MAX_PATH.toUInt()
    ofn.lpstrFilter = "json\u0000*.json\u0000".wcstr.ptr
    ofn.nFilterIndex = 1u
    ofn.lpstrTitle = "选择文件".wcstr.ptr
    ofn.Flags = (OFN_PATHMUSTEXIST or OFN_FILEMUSTEXIST).toUInt()


    if (GetOpenFileNameW(ofn.ptr) != 0) buffer.toKStringFromUtf16() else null
}

fun openChooseFileWindow(onChoose:(String)->Unit) = TopWindow("选择配置文件",M.minSize(400,300), windowProcess = {
    object : WindowProcess by it{
        override fun onDropFile(path: String): Boolean {
            info(111)
            onChoose(path)
            return true
        }
        override fun onClose(): Boolean {
            exit(0)
            error(0)
        }
    }
}){
    val topHwnd = hwnd
    hwnd.dragAcceptFiles(true)
    Column {
        var chosen by mutStateOf("")
        Text(M.height(50).padding(10),A,stateOf("拖拽文件到此处，输入文件路径，或者在下方的按钮中手动选择"),A.left())
        Edit(M.height(30).padding(10),A,extract { chosen }){ chosen = it }
        Row(M.weight(0f)) {
            Spacer(M)
            Button(M.size(80,40).padding(10),A,stateOf("选择")){
                onChoose(chooseFile(topHwnd) ?: return@Button)
                topHwnd.destroy()
            }
            Button(M.size(80,40).padding(10),A,stateOf("确认"),combine { chosen.isNotBlank() }){
                onChoose(chosen)
                topHwnd.destroy()
            }
        }
    }
}

fun Main(args: Array<String>) {
    registerLayered()
    registerGui()

    if(args.getOrNull(0) != null) {
        val container = createContainerFromFilePath(args[0])
        openMainWindow(container)
    } else {
        openChooseFileWindow {
            val container = createContainerFromFilePath(it)
            openMainWindow(container)
        }
    }

    loopWindowMessage()
}
