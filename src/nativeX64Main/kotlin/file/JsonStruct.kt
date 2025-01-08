package file

import button.*
import buttonGroup.*
import container.Container
import draw.Color
import error.groupTypeError
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import touch.TouchReceiver

@Serializable
data class ButtonJson(
    val name:String,
    val rect: Rect,
    val key:List<UByte>,
    override var textColor: Color? = null,
    override var textColorPressed: Color? = null,
    override var color: Color? = null,
    override var colorPressed: Color? = null,
    override var textSize:Byte? = null,
): HasButtonConfigs {
    fun toButton(parentConfigs: HasButtonConfigs):Button {
        val configs = buttonConfigs()
        configs.copyConfig(this)
        configs.copyConfig(parentConfigs)
        return Button(
            name = name,
            rect = rect,
            key = key,
        ).apply { copyConfig(configs) }
    }
}

@Serializable
data class GroupJson(
    val offset: Point,
    val buttons: List<ButtonJson> = emptyList(),
    val type: UByte = 0u,
    override var textColor:Color? = null,
    override var textColorPressed: Color? = null,
    override var color:Color? = null,
    override var colorPressed:Color? = null,
    override var textSize:Byte? = null,
    val sensitivity:Float = 1f,
    val slideCount:UInt = 1u,
    val ms:ULong = 300uL,
    val holdIndex:Int = 0,
): HasButtonConfigs {
    @OptIn(ExperimentalForeignApi::class)
    fun toGroup(parentConfigs: HasButtonConfigs): Group{
        val configs = buttonConfigs().apply { copyConfig(parentConfigs) }
        configs.copyConfig(this)
        configs.copyConfig(parentConfigs)
        val btns = buttons.map { it.toButton(configs) }
        val group:Group = when(type.toInt()){
            0 -> NormalGroup(btns, offset)
            1 -> SlideGroup(btns,offset,slideCount)
            2 -> HoldSlideGroup(btns,offset,holdIndex)
            3 -> HoldGroup(btns,offset)
            4 -> HoldGroupDoubleClk(btns,offset,ms)
            8 -> MouseGroup(btns,offset,sensitivity)
            9 -> ScrollGroup(btns,offset,sensitivity)
            else -> groupTypeError(type)
        }
        return group.apply { copyConfig(configs) }
    }
}

@Serializable
data class ContainerJson(
    val alpha:UByte = 128u,
    val groups:List<GroupJson> = emptyList(),
    override var textColor:Color? = null,
    override var textColorPressed: Color? = null,
    override var color:Color? = null,
    override var colorPressed:Color? = null,
    override var textSize:Byte? = null
): HasButtonConfigs {
    @OptIn(ExperimentalForeignApi::class)
    fun toContainer():Container {
        return Container(
            alpha = alpha,
            groups = groups.map { it.toGroup(this) }
        ).apply { copyConfig(this@ContainerJson) }
    }
}