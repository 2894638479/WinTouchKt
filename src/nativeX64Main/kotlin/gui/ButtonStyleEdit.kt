package gui

import dsl.*
import geometry.Color
import geometry.Font
import node.ButtonStyle
import node.Node
import wrapper.Delegate

fun GuiScope.ButtonStyleEdit(modifier: Modifier = M, alignment: Alignment = A, display:State<Node.DisplayStyle>,style:State<ButtonStyle>){
    val display by display
    val style by style

    context(guiScope: GuiScope)
    fun windowsByStyle() = scopeWindows {
        destroyOnChange += guiScope.extract { style }
    }

    fun GuiScope.Color(text:String,display:State<Color>,get:State<Color?>,set:(Color?)->Unit){
        var color by Delegate(get,set)
        Column {
            Text(M.minHeight(25).padding(5),A,stateOf(text))
            Box(M.minHeight(25).padding(5),color = display){}
            Row(M.padding(5)) {
                If(combine { color != null }) {
                    context(windowsByStyle()){
                        EditButton(M.padding(right = 10)) {
                            Window("编辑颜色", M.minSize(400,300)) {
                                ColorEdit(M.padding(10), A, combine { color!! }) { color = it }
                            }
                        }
                    }
                    DefaultButton { color = null }
                } Else {
                    CreateButton { color = display.value }
                }
            }
        }
    }


   Column(modifier,alignment) {
       Row {
           Color("颜色",combine { display.color },combine { style.color }){style.color = it}
           Color("边框颜色",combine { display.outlineColor },combine { style.outlineColor }){style.outlineColor = it}
           Color("文字颜色",combine { display.textColor },combine { style.textColor }){style.textColor = it}
       }
       Row {
           Column {
               Text(M,A,stateOf("字体"))
               Edit(M,A,combine { style.fontFamily ?: "" }){ style.fontFamily = it }
               Row {
                   context(windowsByStyle()){
                       EditButton {
                           Window("编辑字体",M.minSize(200,300)){
                               ScrollableColumn {
                                   Font.systemFontFamilies.forEach {
                                       Button(M.minHeight(30).padding(5),A,stateOf(it)){
                                           style.fontFamily = it
                                       }
                                   }
                               }
                           }
                       }
                   }
                   DefaultButton(active = combine { style.fontFamily != null }) { style.fontFamily = null }
               }
           }
           Column {
               Text(M,A,stateOf("字体大小"))
               EditFloat(M,A,combine { style.fontSize }){
                   style.fontSize = it
               }
           }
       }
   }
}