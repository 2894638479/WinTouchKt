package gui

import dsl.A
import dsl.GuiScope
import dsl.M
import dsl.Modifier
import dsl.height
import dsl.middleY
import dsl.mutStateOf
import dsl.padding
import dsl.right
import dsl.size
import dsl.width
import kotlinx.cinterop.ExperimentalForeignApi
import node.Button
import node.Container
import node.Group
import node.Node


@OptIn(ExperimentalForeignApi::class)
fun GuiScope.MainContent(container: Container) = Row {
    var node by container::selected
    fun GuiScope.NodeButton(modifier: Modifier,buttonNode:Node){
        Button(
            modifier.padding(5).height(40),
            text = combine {
                val prefix = when(buttonNode::class){
                    Container::class ->"配置："
                    Group::class -> "分组："
                    Button::class -> "按钮："
                    else -> "未知："
                }
                prefix + (buttonNode.name ?: "")
            },
            enable = combine { buttonNode != node }
        ){ node = buttonNode }
    }
    ScrollableColumn(M.weight(1f)) {
        NodeButton(M,container)
        List(container.children){
            Column {
                var fold by mutStateOf(false)
                Row {
                    Button(M.size(30,30).padding(5),A.middleY(),combine { if(fold) ">" else "v" }){ fold = !fold }
                    NodeButton(M.weight(1f),it)
                }
                By(extract{ fold }){ fold ->
                    if(!fold) List(it.children){
                        NodeButton(M.padding(left = 40),it)
                    }
                }
            }
        }
    }
    ScrollableColumn(M.weight(2f)) {
        Node(extract { node })
    }
}