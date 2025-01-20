package json

import kotlinx.serialization.Serializable

@Serializable
class ContainerJson(
    val alpha:UByte = 128u,
    val scale:Float = 1f,
    val groups:List<GroupJson> = emptyList(),
    val style: ButtonStyleJson? = null,
    val stylePressed: ButtonStyleJson? = null,
    val outlineWidth:Float? = null
)