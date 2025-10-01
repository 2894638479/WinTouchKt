package gui.window

import chooseSaveFile
import dsl.A
import dsl.M
import dsl.TopWindow
import dsl.left
import dsl.minSize
import dsl.padding
import dsl.size
import dsl.stateOf
import error.exitProcess
import node.Container

fun openExitWindow(container:Container){
    TopWindow("是否退出程序?",M.minSize(400,200)){
        Column {
            Text(M.padding(10),A,stateOf("如果直接退出程序，对配置的修改将不会保存"),A.left())
            Row(M.weight(0f)) {
                Button(M.size(80,40).padding(10),A,stateOf("编辑配置")){
                    openMainWindow(container)
                    this@TopWindow.hwnd.destroy()
                }
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