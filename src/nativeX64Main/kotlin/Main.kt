import container.Container
import draw.DrawScope
import file.readContainer
import kotlinx.cinterop.ExperimentalForeignApi

private var drawScopeRaw: DrawScope? = null
private var mainContainerRaw: Container? = null
val drawScope get() = drawScopeRaw!!
val mainContainer get() = mainContainerRaw!!


@OptIn(ExperimentalForeignApi::class)
fun main(args:Array<String>) {
    mainContainerRaw = readContainer(args)
    window { hWnd ->
        drawScopeRaw = DrawScope(hWnd)
        mainContainer.invalidate = drawScope::invalidate
        drawScope.addButtons(mainContainer::forEachButton)
        drawScope.alpha = mainContainer.alpha
        println("initialized")
    }
}