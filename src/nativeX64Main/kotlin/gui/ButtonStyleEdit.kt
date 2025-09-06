package gui

import dsl.*
import geometry.Color
import node.ButtonStyle
import node.Node
import wrapper.Delegate

fun GuiScope.ButtonStyleEdit(modifier: Modifier = M, alignment: Alignment = A, display:State<Node.DisplayStyle>,style:State<ButtonStyle>){
    val display by display
    val style by style

    context(wm:WindowManager)
    fun GuiScope.Color(text:String,display:State<Color>,get:State<Color?>,set:(Color?)->Unit){
        var color by Delegate(get,set)
        Column {
            Text(M.minHeight(25).padding(5),A,stateOf(text))
            Box(M.minHeight(25).padding(5),color = display){}
            Row(M.padding(5)) {
                If(combine { color != null }) {
                    EditButton(M.padding(right = 10)) {
                        Window("编辑颜色", M.minSize(400,300)) {
                            ColorEdit(M.padding(10), A, combine { color!! }) { color = it }
                        }
                    }
                    DefaultButton { color = null }
                } Else {
                    CreateButton { color = display.value }
                }
            }
        }
    }

    val styleEditWindows = scopeWindows {
        destroyOnChange += extract { style }
    }

    context(styleEditWindows){
        Row(modifier.padding(5),alignment) {
            Color("颜色",combine { display.color },combine { style.color }){style.color = it}
            Color("边框颜色",combine { display.outlineColor },combine { style.outlineColor }){style.outlineColor = it}
            Color("文字颜色",combine { display.textColor },combine { style.textColor }){style.textColor = it}
        }
    }
}