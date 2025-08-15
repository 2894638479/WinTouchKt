package gui

import dsl.A
import dsl.Alignment
import dsl.GuiScope
import dsl.M
import dsl.Modifier
import dsl.State
import dsl.minHeight
import dsl.minWidth
import dsl.padding
import geometry.Point
import wrapper.Delegate

fun GuiScope.PointEdit(modifier: Modifier = M, alignment: Alignment = A,get:State<Point?>,set:(Point?)->Unit){
    Row(modifier,alignment) {
        var point by Delegate(get,set)
        EditFloat(M.padding(right = 5).weight(1f),get = combine { point?.x }){
            if(it != null || point != null) point = Point(it?:0f,point?.y?:0f)
        }
        EditFloat(M.padding(h = 5).weight(1f),get = combine { point?.y }){
            if(it != null || point != null) point = Point(point?.x?:0f,it?:0f)
        }
        DefaultButton(M.padding(left = 5).weight(0.5f), active = combine { point != null }){ point = null }
    }
}