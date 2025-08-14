package gui

import dsl.Alignment
import dsl.GuiScope
import dsl.Modifier
import dsl.State
import dsl.height
import geometry.Point

fun GuiScope.PointEdit(modifier: Modifier, alignment: Alignment, get: State<Point?>, set:(Point?)->Unit){
    Row(modifier,alignment) {
//        Edit(Modifier().height(20))
    }
}