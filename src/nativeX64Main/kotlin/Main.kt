import dsl.*
import error.catchInKotlin
import error.wrapExceptionName
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import libs.Clib.GBKToUTF8
import libs.Clib.PrepareForUIAccess
import libs.Clib.freeStr
import logger.warning
import node.Container
import window.loopWindowMessage
import window.registerGui
import window.registerLayered


@OptIn(ExperimentalForeignApi::class)
fun main() = catchInKotlin {
    PrepareForUIAccess()
    val argc = platform.posix.__argc
    val argv = platform.posix.__argv
    Main(Array(argc - 1){ i ->
        val gbk = argv?.get(i + 1) ?: error("argv is null")
        val utf8 = GBKToUTF8(gbk) ?: error("utf8 convert error")
        val str =  utf8.toKString()
        freeStr(utf8)
        str
    }).run { Unit }
}
fun Main(args: Array<String>) = processArgs(args).apply {
    registerLayered()
    registerGui()
    val container = wrapExceptionName("json decode failed"){
        json.decodeFromString<Container>(jsonStr)
    }
    warning(container.toString())
    val hwndLayered = container.drawScope.hwnd
    hwndLayered.showAndUpdate()

    val scope = MutState.SimpleScope()
    class A{
        val m1 = mutStateOf(0)
        val m2 = mutStateOf("test")
    }
    val a = mutStateOf(A())
    val b = mutStateOf(4)

    val combined1 = scope.combine {
        "${a.track.m2.track} ${b.track}"
    }
    val combined2 = scope.combine {
        "${a.track.m1.track} ${a.track.m2.track}"
    }
    val combined3 = scope.combine {
        "${a.track.m1.track} ${a.track.m2.track} ${b.track}"
    }
    scope.run {
        combined1.listen { warning("combined1 $it") }
        combined2.listen { warning("combined2 $it") }
        combined3.listen { warning("combined3 $it") }
    }


    TopWindow("window",800,600){
        ScrollableColumn {
            Button(Modifier().weight(1f).width(200).minHeight(200),Alignment().middleX(),combine { a.track.m2.track + "0" }){
                a.value.m1.value++
            }
            Button(Modifier().weight(3f).width(200),Alignment().middleX(),combine { a.track.m2.track  + "1"}){
                a.value.m1.value++
            }
            Button(Modifier().weight(2f).width(200).padding(150),Alignment().middleX(),combine { a.track.m2.track + "2" }){
                a.value.m1.value++
            }
            val list = mutStateList("aaa","bbb","ccc")
            Row {
                Button(Modifier().weight(1f).minHeight(200),Alignment().middleX(),combine { a.track.m2.track + "0" }){
                    a.value.m1.value++
                    list.removeAt(0)
                }
                Button(Modifier().weight(3f),Alignment().middleX(),combine { a.track.m2.track  + "1"}){
                    a.value.m1.value++
                    list.add("${list.size}")
                }
                Button(Modifier().weight(2f),Alignment().middleX(),combine { a.track.m2.track + "2" }){
                    a.value.m1.value++
                }
            }
            List(list){
                Text(Modifier().height(200), Alignment().middleX(),stateOf(it))
            }
        }
    }
    loopWindowMessage()
}
