package draw

import button.Button

interface DrawScope {
    var alpha:UByte
    val showStatus:Boolean
    fun onDraw()
    fun resize()
    fun invalidate(button:Button)
    fun hideButtons(controller: Button)
    fun showButtons()
    val iterateButtons:((Button)->Unit)->Unit
    fun destruct()
}