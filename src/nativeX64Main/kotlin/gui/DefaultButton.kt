package gui

import dsl.Alignment
import dsl.GuiScope
import dsl.Modifier
import dsl.State
import dsl.height
import dsl.middleY
import dsl.minHeight
import dsl.minWidth
import dsl.padding
import dsl.stateOf

fun GuiScope.DefaultButton(modifier: Modifier = Modifier(), alignment: Alignment = Alignment(),
         active: State<Boolean>,onClick:()->Unit){
    Button(modifier.minHeight(30).minWidth(50), Alignment().middleY(),
        stateOf("默认"),active,onClick)
}