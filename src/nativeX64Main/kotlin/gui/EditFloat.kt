package gui

import dsl.Alignment
import dsl.GuiScope
import dsl.Modifier
import dsl.State
import dsl.height
import dsl.middleY
import dsl.padding
import dsl.stateOf

fun GuiScope.EditFloat(modifier:Modifier,alignment:Alignment,get:State<Float?>,set:(Float?)->Unit){
    Row(modifier,alignment) {
        val float by get
        val text = combine { float?.toString() ?: "" }
        var last = text.value
        Edit(Modifier().height(20).weight(2f),Alignment().middleY(),text){
            it.ifBlank {
                set(null)
                last = ""
                return@Edit
            }
            it.toFloatOrNull()?.let {
                set(it)
                last = text.value
            } ?: run { text.value = last }
        }
        DefaultButton(active =  combine{ float != null }){ set(null) }
    }
}