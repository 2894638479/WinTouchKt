package gui

import dsl.A
import dsl.Alignment
import dsl.GuiScope
import dsl.M
import dsl.Modifier
import dsl.State
import dsl.middle
import dsl.minHeight
import dsl.minWidth
import dsl.stateOf

fun GuiScope.DefaultButton(
    modifier: Modifier = M, alignment: Alignment = A,
    active: State<Boolean> = stateOf(true),onClick:()->Unit
){
    Button(
        modifier.minHeight(25).minWidth(50), alignment.middle(),
        stateOf("默认"),active,onClick
    )
}

fun GuiScope.CreateButton(
    modifier: Modifier = M, alignment: Alignment = A,
    active: State<Boolean> = stateOf(true),onClick:()->Unit
) {
    Button(
        modifier.minHeight(25).minWidth(50), alignment.middle(),
        stateOf("新建"), active, onClick
    )
}

fun GuiScope.EditButton(
    modifier: Modifier = M, alignment: Alignment = A,
    active: State<Boolean> = stateOf(true),onClick:()->Unit
) {
    Button(
        modifier.minHeight(25).minWidth(50), alignment.middle(),
        stateOf("编辑"), active, onClick
    )
}