import dsl.*
import error.catchInKotlin
import error.exitProcess
import error.wrapExceptionName
import gui.MainContent
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import libs.Clib.GBKToUTF8
import libs.Clib.freeStr
import node.Container
import window.loopWindowMessage
import window.registerGui
import window.registerLayered
import wrapper.WindowProcess


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

fun openExitWindow(container:Container){
    TopWindow("是否退出程序?",M.minSize(400,200)){
        Column {
            Text(M.padding(10),A,stateOf("如果直接退出程序，对配置的修改将不会保存"),A.left())
            Row(M.weight(0f)) {
                Spacer(M)
                Button(M.size(80,40).padding(10),A,stateOf("保存并退出")){
                    if(container.saveToFile()) exitProcess(0)
                }
                Button(M.size(80,40).padding(10),A,stateOf("另存并退出")){
                    val path = chooseSaveFile(hwnd) ?: return@Button
                    if(container.saveToFile(path)) exitProcess(0)
                }
                Button(M.size(80,40).padding(10),A,stateOf("直接退出")){
                    exitProcess(0)
                }
            }
        }
    }
}

fun openProtectWindow(container: Container) = TopWindow("WinTouchKt运行中",M.minSize(400,200), windowProcess = {
    object : WindowProcess by it {
        override fun onClose(): Boolean {
            openExitWindow(container)
            return true
        }
    }
}){
    val topWindow = hwnd
    Column {
        Text(M.padding(10),A,stateOf("WinTouchKt运行中，可以最小化此窗口"),A.left())
        Text(M.padding(10),A,stateOf("当前文件：${container.filePath}"),A.left())
        Row(M.weight(0f)) {
            Spacer(M)
            Button(M.size(80,40).padding(10),A,combine { if(container.drawScope.showStatus) "隐藏按钮" else "显示按钮" }){
                container.drawScope.run { showStatus = !showStatus }
            }
            Button(M.size(80,40).padding(10),A,stateOf("编辑配置")){
                openMainWindow(container)
            }
            Button(M.size(80,40).padding(10),A,stateOf("退出")){
                topWindow.close()
            }
        }
    }
}

fun openMainWindow(container: Container) = TopWindow("配置主界面", M.minSize(800,600), windowProcess = {
    object : WindowProcess by it {
        override fun onClose(): Boolean {
            container.closeConfig()
            return false
        }
    }
}) {
    wrapExceptionName("creating MainContent") {
        MainContent(container)
    }
}


fun openChooseFileWindow(onCreate:(String)->Unit,onChoose:(String)->Unit) = TopWindow("选择配置文件",M.minSize(400,200), windowProcess = {
    object : WindowProcess by it{
        override fun onDropFile(path: String): Boolean {
            onChoose(path)
            return true
        }
        override fun onClose(): Boolean {
            exitProcess(0)
        }
    }
}){
    val topHwnd = hwnd
    hwnd.dragAcceptFiles(true)
    Column {
        var chosen by mutStateOf("")
        Text(M.padding(10),A,stateOf("拖拽文件到此处，或者输入文件路径，或者在下方的按钮中手动选择。也可以在启动软件时直接将文件拖到exe上"),A.left())
        Edit(M.height(30).padding(10),A,extract { chosen }){ chosen = it }
        Row(M.weight(0f)) {
            Spacer(M)
            Button(M.size(80,40).padding(10),A,stateOf("新建")){
                onCreate(chooseSaveFile(topHwnd) ?: return@Button)
                topHwnd.destroy()
            }
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
        openProtectWindow(container)
    } else {
        openChooseFileWindow({
            val container = Container().apply { filePath = it }
            openProtectWindow(container)
        }) {
            val container = createContainerFromFilePath(it)
            openProtectWindow(container)
        }
    }

    loopWindowMessage()
}
