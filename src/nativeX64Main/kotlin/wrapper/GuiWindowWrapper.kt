package wrapper

import error.catchInKotlin
import kotlinx.cinterop.*
import logger.info
import logger.warning
import platform.windows.*
import window.showWindow


@OptIn(ExperimentalForeignApi::class)
fun wndProcGui(hWnd: HWND?, uMsg: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT = catchInKotlin {
    val guiWindow = dataMap[Hwnd(hWnd)] ?: creatingGuiWindow ?: error("gui window not found")
    when(uMsg.toInt()){
        WM_CREATE -> {
        }
        WM_SIZE -> {
            info("gui window size changed to ${Hwnd(hWnd).size.str()}")
            guiWindow.onSize()
        }
        WM_GETMINMAXINFO -> {
            val ptr = lParam.toCPointer<MINMAXINFO>()
            ptr?.pointed?.apply {
                ptMinTrackSize.x = guiWindow.minW
                ptMinTrackSize.y = guiWindow.minH
            } ?: warning("gui window min size set failed")
        }
        WM_COMMAND -> {
            fun LOWORD(value: Int) = value and 0xFFFF
            fun HIWORD(value: Int) = (value shr 16) and 0xFFFF
            val id = LOWORD(wParam.toInt()).toUShort()
            val notificationCode = HIWORD(wParam.toInt())
            guiWindow.onWmCommand(id,notificationCode)
        }
        else -> return DefWindowProcW(hWnd, uMsg, wParam, lParam)
    }
    return 0
}


private val dataMap = mutableMapOf<Hwnd,GuiWindow>()
const val guiWindowClass = "gui_window"
private var creatingGuiWindow:GuiWindow? = null


@OptIn(ExperimentalForeignApi::class)
abstract class GuiWindow (
    val windowName:String,
    val minW:Int, val minH:Int,
){
    val hwnd:Hwnd
    init {
        creatingGuiWindow = this
        hwnd = CreateWindowExW(
            (WS_EX_TOPMOST).toUInt(),
            guiWindowClass, windowName, WS_OVERLAPPEDWINDOW.toUInt(),
            CW_USEDEFAULT, CW_USEDEFAULT, minW,minH,null,null, GetModuleHandleW(null), null
        ).also { if(it == null) error("gui window create failed") }.let { Hwnd(it) }
        creatingGuiWindow = null
        dataMap[hwnd] = this
    }
    val relativeRect get() = hwnd.size.apply { toOrigin() }
    fun show() = showWindow(hwnd)
    abstract fun onSize()

    fun onWmCommand(id:UShort,nCode:Int){
        when(nCode){
            BN_CLICKED -> {
                onClicks[id]?.invoke() ?: error("no onClick found for id")
            }
        }
    }



    private var idIncrease = 100.toUShort()
        get() = field++
    private val onClicks = mutableMapOf<UShort,()->Unit>()


    abstract class GuiItem(val hwnd: Hwnd){
        fun move(x:Int,y:Int,w:Int,h:Int){
            info("changing gui item size to $x $y   $w $h")
            MoveWindow(hwnd.HWND,x,y,w,h,TRUE).ifFalse { warning("gui item move false") }
        }
        fun moveRect(rect: tagRECT) = rect.run { move(left,top,right - left,bottom - top) }
    }
    class Button(hwnd:Hwnd,val id:UShort):GuiItem(hwnd)

    @OptIn(ExperimentalForeignApi::class)
    fun button(string:String = "Click Me!", onClick:()->Unit):Button {
        val id = idIncrease
        val hwnd = CreateWindowExW(
            0u,
            "BUTTON",
            string,
            (WS_TABSTOP or WS_VISIBLE or WS_CHILD or BS_DEFPUSHBUTTON).toUInt(),
            100, 50, 100, 30,
            hwnd.HWND,
            id.toLong().toCPointer(),
            GetModuleHandleW(null),
            null
        ).let { Hwnd(it) }
        onClicks[id] = onClick
        return Button(hwnd, id)
    }
}