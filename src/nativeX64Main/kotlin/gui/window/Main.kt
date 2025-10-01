package gui.window

import dsl.M
import dsl.TopWindow
import dsl.minSize
import dsl.size
import error.wrapExceptionName
import gui.MainContent
import node.Container
import wrapper.WindowProcess

fun openMainWindow(container: Container) = TopWindow("配置主界面", M.minSize(800,300).size(800,600), windowProcess = {
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