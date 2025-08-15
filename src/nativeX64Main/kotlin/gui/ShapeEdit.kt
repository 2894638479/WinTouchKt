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
    info("shape1")
    Column(modifier,alignment) {
        info("shape2")
        By(combine { shape::class }) {
            when(it) {
                Rect::class -> {
                    Row(M.height(40),A.left()){
                        Text(M.size(20,20),A.middleY(), stateOf("宽"))
                        EditFloat(M,A,combine { (shape as Rect).width }){
                            if(it != null) shape = (shape as Rect).run{ Rect(it,height) }
                        }
                    }
                }
                RoundedRect::class -> {
                    Row(M.height(40),A.left()){
                        Text(M.size(20,20).padding(h = 5),A.middleY(), stateOf("width"))
                        EditFloat(M.height(20).padding(h = 5),A.middleY(),combine { (shape as RoundedRect).width }){
                            if(it != null) shape = (shape as RoundedRect).run{ RoundedRect(it,height,r) }
                        }
                    }
                }
                Round::class -> {
                }
            }
        }
    }
}