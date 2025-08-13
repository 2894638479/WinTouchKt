package gui

import dsl.Alignment
import dsl.GuiScope
import dsl.Modifier
import dsl.MutState
import dsl.height
import dsl.padding
import dsl.stateOf

fun GuiScope.EditFloat(modifier: Modifier,alignment: Alignment,floatState: MutState<Float?>){
    Row(modifier,alignment) {
        var float by floatState
        val text = combine { float?.toString() ?: "" }
        var last = text.value
        Edit(Modifier().height(30).weight(2f),Alignment(),text){
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
        Button(Modifier().height(30).weight(1f).padding(left = 10), Alignment(), stateOf("默认"),combine{ float != null }){
            float = null
        }
    }
}