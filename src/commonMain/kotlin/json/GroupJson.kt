package json

import kotlinx.serialization.Serializable

@Serializable
data class GroupJson(
    val offset: PointJson,
    val buttons: List<ButtonJson> = emptyList(),
    val type: UByte = 0u,
    val sensitivity:Float = 1f,
    val slideCount:UInt = 1u,
    val ms:ULong = 300uL,
    val holdIndex:Int = 0,
    val style: ButtonStyleJson? = null,
    val stylePressed: ButtonStyleJson? = null,
)