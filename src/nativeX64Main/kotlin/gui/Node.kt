package gui

import dsl.Alignment
import dsl.GuiScope
import dsl.Modifier
import dsl.MutState
import dsl.height
import dsl.middleY
import dsl.padding
import dsl.stateOf
import logger.info
import node.Node

fun GuiScope.Node(nodeState: MutState<Node>){
    var node by nodeState
    Row(Modifier().height(50)){
        Text(Modifier().height(20).padding(horizontal = 5).weight(0.3f), Alignment().middleY(), stateOf("名称:"))
        Edit(Modifier().height(20).padding(horizontal = 5).weight(1f),Alignment().middleY(),combine{node.name ?: ""}){
            node.name = it
            info("$it $node")
        }
        Text(Modifier().height(20).padding(horizontal = 5).weight(0.3f), Alignment().middleY(), stateOf("缩放:"))
        EditFloat(Modifier().padding(horizontal = 5).weight(1f),Alignment().middleY(),combine { node.scale }){node.scale = it}
    }
    Row(Modifier().height(50).padding(top = 10)){
        Text(Modifier().height(20).padding(horizontal = 5).weight(0.3f), Alignment().middleY(), stateOf(":"))
//        EditFloat(Modifier().padding(horizontal = 5).weight(1f), Alignment().middleY(),)
    }
}