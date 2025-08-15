package gui

import dsl.*
import wrapper.Delegate

fun GuiScope.EditFloat(modifier:Modifier = M, alignment:Alignment = A, get:State<Float?>, set:(Float?)->Unit){
    var float by Delegate(get,set)
    val text = combine { float?.toString() ?: "" }
    var last = text.value
    Edit(modifier.minHeight(20).minWidth(40),alignment.middleY(),text){
        it.ifBlank {
            float = null
            last = ""
            return@Edit
        }
        it.toFloatOrNull()?.let {
            float = it
            last = text.value
        } ?: run { text.value = last }
    }
}