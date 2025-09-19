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
import dsl.padding
import dsl.size
import dsl.stateOf
import geometry.Point
import wrapper.Delegate

fun GuiScope.PointEdit(modifier: Modifier = M, alignment: Alignment = A,get:State<Point?>,set:(Point?)->Unit){
    Column(modifier,alignment) {
        var point by Delegate(get,set)
        Row {
            Text(M.size(25,25),A.middle(), stateOf("x"))
            EditFloat(get = combine { point?.x }){
                if(it != null || point != null) point = Point(it?:0f,point?.y?:0f)
            }
        }
        Row {
            Text(M.size(25,25),A.middle(), stateOf("y"))
            EditFloat(get = combine { point?.y }){
                if(it != null || point != null) point = Point(point?.x?:0f,it?:0f)
            }
        }
        DefaultButton(M.weight(0.5f), active = combine { point != null }){ point = null }
    }
}