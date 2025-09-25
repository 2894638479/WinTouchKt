package gui

import dsl.A
import dsl.GuiScope
import dsl.M
import dsl.height
import dsl.State
import dsl.padding
import dsl.stateOf
import dsl.width
import node.Button
import node.ButtonStyle
import node.Container
import node.Node

fun GuiScope.Node(get:State<Node>) = Column {
    val node by get
    val scale by combine { node.scale }
    Row(M.padding(10)){
        Column(M.padding(h = 5)) {
            Text(M.padding(bottom = 10),A, stateOf("名称"))
            Edit(M,A,combine{ node.name ?: ""}){ node.name = it.takeIf { it.isNotBlank() } }
        }
        Column {
            Row {
                Text(M.padding(h = 5).weight(0.3f),A, stateOf("缩放"))
                EditFloat(M.padding(h = 5).weight(1f),A,extract { scale }){ node.scale = it }
                DefaultButton(M.padding(h = 5).weight(0.3f),active = combine{ scale != null }){ node.scale = null }
            }
            TrackBar(M.padding(h = 5),A,combine { scale ?: 1f },0.2f..5f){
                node.scale = it
            }
        }
    }
    Row(M.padding(10)) {
        Row {
            Text(M.padding(h = 5).width(60),A,stateOf("位置"))
            PointEdit(M.padding(h = 5),A,combine { node.offset }){node.offset = it}
        }
        If(combine { node is Container }){
            val alpha by combine { (node as Container).alpha }
            Column {
                Row {
                    Text(M.padding(h = 5).width(120),A, stateOf("不透明度"))
                    Edit(M.padding(h = 5).weight(1f),A,combine { alpha?.toString() ?: "" }){ (node as Container).alpha = it.toUByteOrNull() }
                    DefaultButton(M.padding(h = 5).weight(0.3f),active = combine{ alpha != null }){ (node as Container).alpha = null }
                }
                TrackBar(M.padding(h = 5),A,combine { alpha?.toInt() ?: 128 },0..255,255){
                    (node as Container).alpha = it.toUByte()
                }
            }
        } Else {
            Spacer(M)
        }
    }

    Spacer(M.height(20))

    Text(M.padding(5).padding(top = 10).padding(h = 10),A,combine { "松开时样式" + (node.style?.let {""} ?: "(默认)") })
    Row {
        If(combine { node.style != null }){
            Column {
                ButtonStyleEdit(M.padding(h = 10),A,combine { node.displayStyle },combine { node.style!! })
                DefaultButton(M.padding(5).padding(h = 10)) { node.style = null }
            }
        } Else {
            CreateButton(M.padding(5).padding(h = 10)) { node.style = ButtonStyle() }
        }
    }

    Spacer(M.height(20))

    Text(M.padding(5).padding(top = 10).padding(h = 10),A,combine { "按下时样式" + (node.stylePressed?.let {""} ?: "(默认)") })
    Row {
        If(combine { node.stylePressed != null }){
            Column {
                ButtonStyleEdit(M.padding(h = 10),A,combine { node.displayPressedStyle },combine { node.stylePressed!! })
                DefaultButton(M.padding(5).padding(h = 10)) { node.stylePressed = null }
            }
        } Else {
            CreateButton(M.padding(5).padding(h = 10)) { node.stylePressed = ButtonStyle() }
        }
    }
    By(combine { node::class }){
        if(it == Button::class) {
            ShapeEdit(M.height(1),A,combine { (node as Button).shape }){(node as Button).shape = it}
        }
    }
}