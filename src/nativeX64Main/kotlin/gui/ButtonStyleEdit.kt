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

    val windowsByStyle = scopeWindows {
        destroyOnChange += extract { style }
    }

    fun GuiScope.Color(text:String,display:State<Color>,get:State<Color?>,set:(Color?)->Unit){
        var color by Delegate(get,set)
        Column {
            Text(M.minHeight(25).padding(h = 5),A,stateOf(text))
            Box(M.minHeight(25).padding(h = 5),color = display){}
            Row(M.padding(h = 5)) {
                If(combine { color != null }) {
                    context(windowsByStyle){
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
       Row(M.padding(top = 10)) {
           Column(M.padding(5)) {
               Text(M,A,stateOf("字体"))
               Edit(M,A,combine { style.fontFamily ?: "" }){ style.fontFamily = it.takeIf { it.isNotBlank() } }
               Row {
                   context(windowsByStyle){
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
           Column(M.padding(5)) {
               Text(M,A,stateOf("字体大小"))
               EditFloat(M,A,combine { style.fontSize }){
                   style.fontSize = it
               }
               If(combine { style.fontSize == null }) {
                   CreateButton { style.fontSize = display.fontSize }
               } Else {
                   TrackBar(M,A,combine { style.fontSize!! },1f..100f,){
                       style.fontSize = it
                   }
               }
           }
           Column(M.padding(5)) {
               val defaultString by combine { "跟随默认(${display.fontStyle.string})" }
               Text(M,A,stateOf("字体样式"))
               Text(M,A,combine { style.fontStyle?.string ?: defaultString })
               context(windowsByStyle){
                   EditButton {
                       Window("编辑字体样式",M.minSize(200,200)){
                           Column {
                               Font.Style.entries.forEach {
                                   Button(M.padding(10),A,stateOf(it.string)){
                                       style.fontStyle = it
                                   }
                               }
                               Button(M.padding(10),A, extract { defaultString }){
                                   style.fontStyle = null
                               }
                           }
                       }
                   }
               }
           }
           Column(M.padding(5)) {
               Text(M,A,combine { "字体粗细(${display.fontWeight})" })
               Edit(M,A,combine { style.fontWeight?.toString() ?: "" }){
                   style.fontWeight = it.toIntOrNull()
               }
               DefaultButton(active = combine { style.fontWeight != null }) { style.fontWeight = null }
           }
       }
   }
}