package gui

import dsl.*
import geometry.Color
import wrapper.Delegate

fun GuiScope.ColorEdit(modifier: Modifier = M,alignment: Alignment = A,get: State<Color>,set:(Color)->Unit){
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
    fun GuiScope.Item(name:String,state:State<UByte>,set:(UByte)->Unit){
        var uByte by Delegate(state,set)
        Row {
            Text(name)
            Edit(combine { uByte.toString() }){ uByte = it }
        }
        TrackBar(state){ uByte = it }
    }

    var color by Delegate(get,set)
    Column(modifier,alignment) {
        Item("r",combine { color.r }){color = color.setR(it)}
        Item("g",combine { color.g }){color = color.setG(it)}
        Item("b",combine { color.b }){color = color.setB(it)}
        Box(M.minHeight(20), color = combine { color }){}
    }
}