package gui

import dsl.A
import dsl.GuiScope
import dsl.M
import dsl.MutState
import dsl.height
import dsl.middle
import dsl.middleY
import dsl.padding
import dsl.stateOf
import node.Button
import node.Node

fun GuiScope.Node(nodeState: MutState<Node>){
    var node by nodeState
    Row(M.height(25).padding(10)){
        Text(M.padding(h = 5).weight(0.3f),A.middle(), stateOf("名称"))
        Edit(M.padding(h = 5).weight(1f),A.middle(),combine{ node.name ?: ""}){ node.name = it }
        Text(M.padding(h = 5).weight(0.3f),A.middle(), stateOf("缩放"))
        EditFloat(M.padding(h = 5).weight(1f),A.middle(),combine { node.scale }){ node.scale = it }
        DefaultButton(M.padding(h = 5).weight(0.5f),active = combine{ node.scale != null }){ node.scale = null }
    }
    Row(M.height(25).padding(10)){
        Text(M.padding(h = 5).weight(0.13f),A.middle(),stateOf("位置"))
        PointEdit(M.padding(h = 5).weight(1f),A.middle(),combine { node.offset }){node.offset = it}
    }
    By(combine { node.style != null }){
        if(it) ButtonStyleEdit(M,A,combine { node.style!! })
    }

    By(combine { node::class }){
        if(it == Button::class) {
            ShapeEdit(M.height(200),A,combine { (node as Button).shape }){(node as Button).shape = it}
        }
    }
}