package button

import draw.*
import draw.Store.free
import error.infoBox
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
@OptIn(ExperimentalForeignApi::class)
class ButtonStyle(
    var color: Color? = null,
    var textColor: Color? = null,
    var outlineColor: Color? = null,
    var fontFamily:String? = null,
    var fontSize:Float? = null,
    var fontStyle:String? = null,
    var fontWeight:Int? = null,
    val outlineWidth:Float? = null
){
    @Transient var scale:Float? = null
    companion object {
        val default = ButtonStyle(
            color = GREY_DARK,
            outlineWidth = 1f
        )
        val defaultPressed = ButtonStyle(
            color = GREY_BRIGHT,
            outlineWidth = 1f
        )
    }
    var parents = listOf(this)
        set(value) {
            field = buildList {
                add(this@ButtonStyle)
                addAll(value)
            }
        }
    private inline fun<T> find(default:T,transform: ButtonStyle.() -> T?) = parents.firstNotNullOfOrNull(transform) ?: default
    private inline fun<T> find(transform: ButtonStyle.() -> T?) = parents.firstNotNullOfOrNull(transform)
    val font get() = Store.font(
        Font(find { fontFamily }, find { fontSize }, find { fontStyle }, find { fontWeight }, find{ scale })
    )
    val brushText get() = Store.brush(find(RED){ textColor })
    val brush get() = Store.brush(find(GREY_DARK){color})
    val brushOutline get() = Store.brush(find(WHITE){outlineColor})
}