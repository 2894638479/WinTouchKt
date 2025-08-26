package gui

import dsl.*
import geometry.Color
import wrapper.Delegate

fun GuiScope.ColorEdit(modifier: Modifier = M,alignment: Alignment = A,get: State<Color>,set:(Color)->Unit){
    var color by Delegate(get,set)
    Column(modifier,alignment) {
        fun GuiScope.Text(str: String) = Text(M.size(20,20).padding(5),A.middleY(),text = stateOf(str))
        fun GuiScope.Edit(state: State<String>, set:(UByte)->Unit) = Edit(
            M.padding(5).minHeight(20), A.middleY(), state
        ){ set(it.toUByteOrNull() ?: 255u) }
        Row {
            Text("r")
            Edit(combine { color.r.toString() }){color = color.setR(it)}
        }
        Row {
            Text("g")
            Edit(combine { color.g.toString() }){color = color.setG(it)}
        }
        Row {
            Text("b")
            Edit(combine { color.b.toString() }){color = color.setB(it)}
        }
    }
}