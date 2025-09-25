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
import wrapper.RODelegate
import kotlin.properties.ReadOnlyProperty

fun GuiScope.ShapeEdit(modifier: Modifier = M, alignment: Alignment = A, get: State<Shape>, set:(Shape)->Unit){
    var shape by Delegate(get,set)
    Column(modifier,alignment) {
        Row(M.height(60)) {
            Button(M.padding(10),A,stateOf("矩形"),combine { shape !is Rect }){
                shape = (shape as? RoundedRect)?.run { Rect(width,height) } ?: Rect(100f,100f)
            }
            Button(M.padding(10),A,stateOf("圆角矩形"),combine { shape !is RoundedRect }){
                shape = (shape as? Rect)?.run { RoundedRect(width,height,10f) } ?: RoundedRect(100f,100f,10f)
            }
            Button(M.padding(10),A,stateOf("圆形"),combine { shape !is Round }){
                shape = Round(50f)
            }
        }

        fun GuiScope.ItemEdit(name: String,range: ClosedFloatingPointRange<Float>,get:State<Float>,set:(Float)->Unit) {
            Row {
                Text(M.size(40,25).padding(5),A, stateOf(name))
                EditFloat(M.padding(5),A,get){
                    if(it != null) set(it)
                }
                TrackBar(M.weight(2f),A,get,range){set(it)}
            }
        }
        By(combine { shape::class }) {
            when(it) {
                Rect::class -> {
                    val rect by RODelegate { shape as Rect }
                    Column {
                        ItemEdit("宽",10f..400f,combine { rect.width }){ shape = Rect(it,rect.height) }
                        ItemEdit("高",10f..400f,combine { rect.height }){ shape = Rect(rect.width,it) }
                    }
                }
                RoundedRect::class -> {
                    val roundedRect by RODelegate { shape as RoundedRect }
                    Column {
                        ItemEdit("宽",10f..400f,combine { roundedRect.width }){
                            shape = roundedRect.run { RoundedRect(it,height,r) }
                        }
                        ItemEdit("高",10f..400f,combine { roundedRect.height }){
                            shape = roundedRect.run { RoundedRect(width,it,r) }
                        }
                        ItemEdit("半径",1f..50f,combine { roundedRect.r }){
                            shape = roundedRect.run { RoundedRect(width,height,it) }
                        }
                    }
                }
                Round::class -> {
                    val round by RODelegate { shape as Round }
                    ItemEdit("半径",5f..200f,combine { round.r }){ shape = Round(it) }
                }
            }
        }
    }
}