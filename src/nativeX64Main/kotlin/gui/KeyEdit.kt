package gui

import dsl.A
import dsl.GuiScope
import dsl.M
import dsl.State
import dsl.Window
import dsl.height
import dsl.minSize
import dsl.padding
import dsl.scopeWindows
import dsl.size
import dsl.stateOf
import dsl.width
import sendInput.KeyHandler
import sendInput.Keys
import wrapper.Delegate

fun GuiScope.KeyEdit(get:State<Set<UByte>>,set:(Set<UByte>)->Unit){
    var keys by Delegate(get,set)
    Row {
        Column {
            Text(M,A,stateOf("键值"))
            EditButton {
                with(scopeWindows {}){
                    Window("编辑键值",M.minSize(600,400)){
                        Row {
                            ScrollableColumn(M.width(100)) {
                                By(get){
                                    it.forEach {
                                        Column {
                                            Row {
                                                Text(M.height(25).padding(top = 5),A,stateOf(it.toString()))
                                                Button(M.size(25,25).padding(h = 5),A,stateOf("x")){
                                                    keys = keys - it
                                                }
                                            }
                                            Text(M.height(25).padding(bottom = 5),A,stateOf(Keys.name(it)))
                                        }
                                    }
                                }
                            }
                            ScrollableColumn {
                                KeyHandler.keyCategory.entries.chunked(3).forEach {
                                    Row {
                                        it.forEach {
                                            with(scopeWindows {}){
                                                Button(M.padding(10),A,stateOf(it.value)){
                                                    Window("选择键值",M.minSize(200,300)){
                                                        ScrollableColumn {
                                                            it.key.forEach {
                                                                Button(M.height(40).padding(10),A,stateOf(Keys.name(it))){
                                                                    keys = keys + it
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Column(M.weight(2f)) {
            By(get){
                it.forEach {
                    Row(M.height(20)) {
                        Text(M.padding(h = 5),A,stateOf(it.toString()))
                        Text(M.weight(3f),A,stateOf(Keys.name(it)))
                    }
                }
            }
        }
    }
}