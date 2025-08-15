package gui

import dsl.A
import dsl.Alignment
import dsl.GuiScope
import dsl.M
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


@OptIn(ExperimentalForeignApi::class)
fun GuiScope.MainContent(container: Container) = Row {
    val nodeState = mutStateOf<Node>(container) { warning(it.toString()) }
    var node by nodeState
    ScrollableColumn(M.weight(1f)) {
        Button(M.padding(10).height(50), text = combine { container.name ?: "null" }){
            node = container
        }
        List(container.children){
            Column {
                Button(M.padding(10).padding(left = 30).height(50),A.right(),combine { it.name ?: "null" }){
                    node = it
                }
                List(it.children){
                    Button(M.padding(10).padding(left = 50).height(50),A.right(),combine { it.name ?: "null" }){
                        node = it
                    }
                }
            }
        }
    }
    Column(M.weight(2f)) {
        Node(nodeState)
    }
}