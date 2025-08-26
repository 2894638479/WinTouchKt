package gui

import dsl.A
import dsl.Alignment
import dsl.GuiScope
import dsl.M
import dsl.Modifier
import dsl.State
import dsl.minHeight
import dsl.padding
import dsl.stateOf
import geometry.Color
import geometry.WHITE
import node.ButtonStyle

fun GuiScope.ButtonStyleEdit(modifier: Modifier = M,alignment: Alignment = A,style: State<ButtonStyle>){
    val style by style
    Row(modifier,alignment) {
        Column(M.padding(5).padding(bottom = 20)) {
            Text(M.minHeight(20),A, stateOf("按钮颜色："))
            By(combine { style.color == null }){
                if(it){
                    Button(M.minHeight(20),A,stateOf("创建")){ style.color = WHITE }
                } else {
                    ColorEdit(M.weight(5f),A,combine { style.color!! }){ style.color = it }
                    Button(M.minHeight(20),A,stateOf("默认")){ style.color = null }
                }
            }
        }
        Column {

        }
    }
}