package gui.window

import dsl.A
import dsl.M
import dsl.TopWindow
import dsl.left
import dsl.minSize
import dsl.padding
import dsl.size
import dsl.stateOf
import node.Container
import wrapper.WindowProcess

fun openProtectWindow(container: Container) = TopWindow("WinTouchKt运行中",M.minSize(400,200), windowProcess = {
    object : WindowProcess by it {
        override fun onClose(): Boolean {
            openExitWindow(container)
            return true
        }
    }
}){
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
                this@TopWindow.hwnd.minimize()
            }
            Button(M.size(80,40).padding(10),A,stateOf("退出")){
                this@TopWindow.hwnd.close()
            }
        }
    }
}