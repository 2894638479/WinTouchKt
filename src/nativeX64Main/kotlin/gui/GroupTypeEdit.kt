package gui

import dsl.*
import group.*
import group.GroupType.Companion.type
import touch.GroupTouchDispatcher
import wrapper.Delegate
import wrapper.RODelegate

fun GuiScope.GroupTypeEdit(get:State<GroupTouchDispatcher>,set:(GroupTouchDispatcher)->Unit){
    val dispatcher by Delegate(get,set)
    Column {
        Text(M,A,combine{ dispatcher.type.groupName })
        with(scopeWindows {}){
            EditButton {
                Window("编辑分组",M.minSize(600,400)){
                    Row {
                        ScrollableColumn {
                            GroupType.entries.forEach {
                                Button(M.height(40).padding(5),A,stateOf(it.groupName),combine { dispatcher.type != it }){
                                    set(it.default())
                                }
                            }
                        }
                        ScrollableColumn(M.weight(2f)) {
                            Text(M,A,combine { "模式说明:${dispatcher.type.groupName}" })
                            Text(M.padding(v = 10).height(100),A,combine { dispatcher.type.description },A.left())
                            By(combine { dispatcher.type }) {
                                when(it) {
                                    GroupType.NORMAL -> {}
                                    GroupType.SLIDE ->  {
                                        val group by RODelegate{ dispatcher as SlideGroup }
                                        Text(M,A,stateOf("同时拖动个数"))
                                        Row {
                                            Edit(M,A,combine { group.slideCount.toString() }){group.slideCount = it.toUIntOrNull() ?: group.slideCount}
                                            TrackBar(M.weight(2f),A,combine { group.slideCount.toInt() },1..5){
                                                group.slideCount = it.toUInt()
                                            }
                                        }
                                    }
                                    GroupType.HOLD_SLIDE -> {}
                                    GroupType.HOLD -> {}
                                    GroupType.HOLD_DOUBLE_CLICK -> {
                                        val group by RODelegate { dispatcher as HoldDoubleClickGroup }
                                        Text(M,A,stateOf("双击判定间隔(毫秒)"))
                                        Row {
                                            Edit(M,A,combine { group.ms.toString() }){group.ms = it.toULongOrNull() ?: group.ms}
                                            TrackBar(M.weight(2f),A,combine { group.ms.toLong() },0L..500L){
                                                group.ms = it.toULong()
                                            }
                                        }
                                    }
                                    GroupType.TOUCHPAD -> {
                                        val group by RODelegate { dispatcher as TouchPadGroup }
                                        Text(M,A,stateOf("双击判定间隔(毫秒)"))
                                        Row {
                                            Edit(M,A,combine { group.ms.toString() }){group.ms = it.toULongOrNull() ?: group.ms}
                                            TrackBar(M.weight(2f),A,combine { group.ms.toLong() },0L..500L){
                                                group.ms = it.toULong()
                                            }
                                        }
                                        Text(M,A,stateOf("灵敏度"))
                                        Row {
                                            Edit(M,A,combine { group.sensitivity.toString() }){group.sensitivity = it.toFloatOrNull() ?: group.sensitivity}
                                            TrackBar(M.weight(2f),A,combine { group.sensitivity },0.1f..10f){
                                                group.sensitivity = it
                                            }
                                        }
                                    }
                                    GroupType.MOUSE -> {
                                        val group by RODelegate { dispatcher as MouseGroup }
                                        Text(M,A,stateOf("灵敏度"))
                                        Row {
                                            Edit(M,A,combine { group.sensitivity.toString() }){group.sensitivity = it.toFloatOrNull() ?: group.sensitivity}
                                            TrackBar(M.weight(2f),A,combine { group.sensitivity },0.1f..10f){
                                                group.sensitivity = it
                                            }
                                        }
                                    }
                                    GroupType.SCROLL -> {
                                        val group by RODelegate { dispatcher as ScrollGroup }
                                        Text(M,A,stateOf("灵敏度"))
                                        Row {
                                            Edit(M,A,combine { group.sensitivity.toString() }){group.sensitivity = it.toFloatOrNull() ?: group.sensitivity}
                                            TrackBar(M.weight(2f),A,combine { group.sensitivity },0.1f..10f){
                                                group.sensitivity = it
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