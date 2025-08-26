package gui

import dsl.*
import wrapper.Delegate

fun GuiScope.EditFloat(modifier:Modifier = M, alignment:Alignment = A, get:State<Float?>, set:(Float?)->Unit){
    var float by Delegate(get,set)
    var text by mutCombine { float?.toString() ?: "" }
    var last = text
    Edit(modifier.minHeight(20).minWidth(40),alignment.middleY(),extract { text }){
        it.ifBlank {
            float = null
            last = ""
            return@Edit
        }
        it.toFloatOrNull()?.let {
            float = it
            last = text
        } ?: run { text = last }
    }
}