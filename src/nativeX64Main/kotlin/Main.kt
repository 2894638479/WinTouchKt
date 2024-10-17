import draw.DrawScope
import kotlinx.cinterop.ExperimentalForeignApi
import container.Container
import file.readFile
import kotlinx.serialization.json.Json

private var drawScopeRaw: DrawScope? = null
private var mainContainerRaw: Container? = null
val drawScope get() = drawScopeRaw!!
val mainContainer get() = mainContainerRaw!!


@OptIn(ExperimentalForeignApi::class)
fun main(args:Array<String>) {
    val dataPath = when(args.size){
        0 -> "data.json"
        1 -> args[0]
        else -> {
            println("too many arguments")
            error("too many arguments")
        }
    }
    mainContainerRaw = Json.decodeFromString(readFile(dataPath))
    window { hWnd ->
        drawScopeRaw = DrawScope(hWnd)
        mainContainer.invalidate = drawScope::invalidate
        drawScope.addButtons(mainContainer::forEachButton)
        drawScope.alpha = mainContainer.alpha
        println("initialized")
    }
}