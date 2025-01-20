package json

import container.Container
import error.emptyContainerError
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable

@Serializable
class ContainerJson(
    val alpha:UByte = 128u,
    val scale:Float = 1f,
    val groups:List<GroupJson> = emptyList(),
    val style: ButtonStyleJson? = null,
    val stylePressed: ButtonStyleJson? = null
){
    init {
        if(groups.isEmpty()) emptyContainerError()
    }
    @OptIn(ExperimentalForeignApi::class)
    fun toContainer():Container{
        val style = style?.setDefault(style) ?: ButtonStyleJson()
        val stylePressed = stylePressed?.setDefault(stylePressed) ?: ButtonStyleJson()
        return Container(
            groups.map { it.toGroup(style, stylePressed,scale) },
            alpha
        )
    }
}