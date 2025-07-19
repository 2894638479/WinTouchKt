import dsl.*
import error.catchInKotlin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import libs.Clib.GBKToUTF8
import libs.Clib.PrepareForUIAccess
import libs.Clib.freeStr
import logger.info
import logger.warning
import node.Container
import window.loopWindowMessage
import window.registerGui
import window.registerLayered
import wrapper.GuiWindow


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
    val container = json.decodeFromString<Container>(jsonStr)
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
        "${a.tracked.m2.tracked} ${b.tracked}"
    }
    val combined2 = scope.combine {
        "${a.tracked.m1.tracked} ${a.tracked.m2.tracked}"
    }
    val combined3 = scope.combine {
        "${a.tracked.m1.tracked} ${a.tracked.m2.tracked} ${b.tracked}"
    }
    scope.run {
        combined1.listen { warning("combined1 $it") }
        combined2.listen { warning("combined2 $it") }
        combined3.listen { warning("combined3 $it") }
    }


    TopWindow("window",800,800){
        val state = MutState(false)
        Button(Modifier().width(50).height(50).padding(left = 50, top = 50),Alignment().bottom().right(),combine{a.tracked.m1.tracked.toString()}){
            info("clickkkked!")
            state.value = !state.value
            a.value.m1.value++
        }
        Button(Modifier().width(50).height(50).padding(right = 50, top = 50),Alignment().bottom().right(),stateOf("change a")){
            a.value = A()
        }
        Box(Modifier().width(400).height(400),Alignment()){
            VisibleIf(state) {
                Button(Modifier().width(50).height(50), Alignment().left().middleY(), combine { b.tracked.toString() }) {
                    info("fsdajk")
                    b.value++
                }
                Edit(Modifier().width(300).height(100), Alignment().right().middleY(),combine{a.tracked.m2.tracked}) {
                    a.value.m2.value = it
                }
            }
        }
        VisibleIf(combine{!state.tracked}){
            Text(Modifier().size(100, 50), Alignment().middleX().bottom(), combined1)
        }
    }
    loopWindowMessage()
}
