package gui

import dsl.A
import dsl.Alignment
import dsl.GuiScope
import dsl.M
import dsl.Modifier
import dsl.State
import dsl.height
import dsl.left
import dsl.middleY
import dsl.mutStateNull
import dsl.padding
import dsl.size
import dsl.stateOf
import dsl.width
import geometry.Rect
import geometry.Round
import geometry.RoundedRect
import geometry.Shape
import logger.info
import wrapper.Delegate

fun GuiScope.ShapeEdit(modifier: Modifier = M, alignment: Alignment = A, get: State<Shape>, set:(Shape)->Unit){
    var shape by Delegate(get,set)
    Column(modifier,alignment) {
        Row(M.height(60)) {
            Button(M.padding(10),A,stateOf("矩形"),combine { shape !is Rect }){
                shape = Rect(100f,100f)
            }
            Button(M.padding(10),A,stateOf("圆角矩形"),combine { shape !is RoundedRect }){
                shape = RoundedRect(100f,100f,20f)
            }
            Button(M.padding(10),A,stateOf("圆形"),combine { shape !is Round }){
                shape = Round(50f)
            }
        }
        By(combine { shape::class }) {
            when(it) {
                Rect::class -> {
                    val rect = shape as Rect
                    Row {
                        Text(M.size(25,25).padding(5),A, stateOf("宽"))
                        EditFloat(M.padding(5),A,combine { rect.width }){
                            if(it != null) shape = rect.run{ Rect(it,height) }
                        }
                    }
                }
                RoundedRect::class -> {
                    val roundedRect = shape as RoundedRect
                    Row(M.height(40),A.left()){
                        Text(M.size(20,20).padding(h = 5),A, stateOf("width"))
                        EditFloat(M.height(20).padding(h = 5),A,combine { roundedRect.width }){
                            if(it != null) shape = roundedRect.run{ RoundedRect(it,height,r) }
                        }
                    }
                }
                Round::class -> {
                    val round = shape as Round
//                    Row(M.height())
                }
            }
        }
    }
}