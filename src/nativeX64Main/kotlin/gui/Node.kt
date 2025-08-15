package gui

import dsl.A
import dsl.Alignment
import dsl.GuiScope
import dsl.M
import dsl.Modifier
import dsl.MutState
import dsl.height
import dsl.middleY
import dsl.padding
import dsl.stateOf
import logger.info
import node.Button
import node.Node

fun GuiScope.Node(nodeState: MutState<Node>){
    var node by nodeState
    Row(M.height(50)){
        Text(M.height(20).padding(h = 5).weight(0.3f),A.middleY(), stateOf("名称:"))
        Edit(M.height(20).padding(h = 5).weight(1f),A.middleY(),combine{ node.name ?: ""}){ node.name = it }
        Text(M.height(20).padding(h = 5).weight(0.3f),A.middleY(), stateOf("缩放:"))
        EditFloat(M.padding(h = 5).weight(1f),A.middleY(),combine { node.scale }){ node.scale = it }
        DefaultButton(M.padding(h = 5).weight(0.5f),active = combine{ node.scale != null }){ node.scale = null }
    }
    Row(M.height(50)){
        Text(M.height(20).padding(h = 5).weight(0.13f),A.middleY(),stateOf("位置："))
        PointEdit(M.padding(h = 5).weight(1f),A.middleY(),combine { node.offset }){node.offset = it}
    }
    By(combine { node::class }){
        info("node ${it==Button::class} $it")
        if(it == Button::class) {
            info("node1")
//            Text(M.height(100),A,stateOf("测试"))
            ShapeEdit(M.height(200),A,combine { (node as Button).shape }){(node as Button).shape = it}
        }
    }
}