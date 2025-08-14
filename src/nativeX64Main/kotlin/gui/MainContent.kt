package gui

import dsl.Alignment
import dsl.GuiScope
import dsl.Modifier
import dsl.height
import dsl.mutStateOf
import dsl.padding
import dsl.right
import kotlinx.cinterop.ExperimentalForeignApi
import logger.info
import logger.warning
import node.Container
import node.Node


fun b(a:Any?):(Any?)->Unit{
    fun a(aa:Any?){info(a.toString()) }
    return ::a
}
val b2 = b(2)
@OptIn(ExperimentalForeignApi::class)
fun GuiScope.MainContent(container: Container) = Row {
    val nodeState = mutStateOf<Node>(container) { warning(it.toString()) }
    var node by nodeState
    ScrollableColumn(Modifier().weight(1f)) {
        container.nameState.listen{warning("1")}
        Button(Modifier().padding(10).height(50), text = combine { container.name ?: "null" }){
            node = container
        }
        List(container.children){
            Column {
                Button(Modifier().padding(10).padding(left = 30).height(50),Alignment().right(),combine { it.name ?: "null" }){
                    node = it
                }
                List(it.children){
                    Button(Modifier().padding(10).padding(left = 50).height(50),Alignment().right(),combine { it.name ?: "null" }){
                        node = it
                    }
                }
            }
        }
    }
    Column(Modifier().weight(2f)) {
        Node(nodeState)
    }
}