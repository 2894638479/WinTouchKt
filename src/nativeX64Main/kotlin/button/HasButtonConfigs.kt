package button

import draw.Color


interface HasButtonConfigs {
    var textColor: Color?
    var textColorPressed: Color?
    var color: Color?
    var colorPressed: Color?
    var textSize:Byte?
    fun copyConfig(other:HasButtonConfigs){
        textColor = textColor ?: other.textColor
        textColorPressed = textColorPressed ?: other.textColorPressed
        color = color ?: other.color
        colorPressed = colorPressed ?: other.colorPressed
        textSize = textSize ?: other.textSize
    }
}