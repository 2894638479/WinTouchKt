package gui

import dsl.*
import geometry.Color
import wrapper.Delegate

fun GuiScope.ColorEdit(modifier: Modifier = M,alignment: Alignment = A,get: State<Color>,set:(Color)->Unit){
    var color by Delegate(get,set)
    Column(modifier,alignment) {
        fun GuiScope.Text(str: String) = Text(M.size(20,20).padding(5),A.middleY(),text = stateOf(str))
        fun GuiScope.Edit(state: State<String>, set:(UByte)->Unit) = Edit(
            M.padding(5).minHeight(25), A.middle(), state
        ){ set(it.toUByteOrNull() ?: 255u) }
        fun GuiScope.TrackBar(state: State<UByte>, onChange:(UByte)->Unit){
            val value by state
            TrackBar(M.padding(5).minHeight(25),A.middle(),combine { value.toInt() },0..255,255){
                onChange(it.toUByte())
            }
        }
        Row {
            Text("r")
            Edit(combine { color.r.toString() }){ color = color.setR(it) }
        }
        TrackBar(combine { color.r }){ color = color.setR(it) }
        Row {
            Text("g")
            Edit(combine { color.g.toString() }){color = color.setG(it)}
        }
        TrackBar(combine { color.g }){ color = color.setG(it) }
        Row {
            Text("b")
            Edit(combine { color.b.toString() }){color = color.setB(it)}
        }
        TrackBar(combine { color.b }){ color = color.setB(it) }
        Box(M.minHeight(20), color = combine { color }){}
    }
}