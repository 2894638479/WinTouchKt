package gui

import dsl.A
import dsl.Alignment
import dsl.GuiScope
import dsl.M
import dsl.Modifier
import dsl.State
import dsl.middle
import dsl.middleY
import dsl.minHeight
import dsl.minWidth
import dsl.stateOf

fun GuiScope.DefaultButton(modifier: Modifier = M, alignment: Alignment = A,
         active: State<Boolean>,onClick:()->Unit){
    Button(modifier.minHeight(25).minWidth(50), alignment.middle(),
        stateOf("默认"),active,onClick)
}