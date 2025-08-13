package gui

import dsl.Alignment
import dsl.GuiScope
import dsl.Modifier
import dsl.height
import dsl.middleY
import dsl.mutStateOf
import dsl.padding
import dsl.right
import logger.info
import node.Container
import node.Node

fun GuiScope.MainContent(container: Container) = Row {
    var node by mutStateOf<Node>(container)
    ScrollableColumn {
        Button(Modifier().padding(10).height(50),text = combine { container.name ?: "null" }){
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
        Row(Modifier().height(50)){
            Edit(Modifier().height(20).padding(),Alignment().middleY(),combine{node.name ?: ""}){
                info("1")
                node.name = it
                info("2")
            }
            EditFloat(Modifier().padding(left = 10),Alignment().middleY(),node.scaleState)
        }
    }
}