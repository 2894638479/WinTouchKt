package gui.window

import chooseFile
import chooseSaveFile
import dsl.A
import dsl.M
import dsl.TopWindow
import dsl.height
import dsl.left
import dsl.minSize
import dsl.mutStateOf
import dsl.padding
import dsl.size
import dsl.stateOf
import error.exitProcess
import wrapper.WindowProcess

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
    hwnd.dragAcceptFiles(true)
    Column {
        var chosen by mutStateOf("")
        Text(M.padding(10),A,stateOf("拖拽文件到此处，或者输入文件路径，或者在下方的按钮中手动选择。也可以在启动软件时直接将文件拖到exe上"),A.left())
        Edit(M.height(30).padding(10),A,extract { chosen }){ chosen = it }
        Row(M.weight(0f)) {
            val topHwnd = this@TopWindow.hwnd
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