package wrapper

import error.catchInKotlin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCPointer
import logger.info
import logger.warning
import platform.windows.*
import kotlin.collections.mutableMapOf
import kotlin.collections.set


@OptIn(ExperimentalForeignApi::class)
fun wndProcGui(hWnd: HWND?, uMsg: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT = catchInKotlin {
    val guiWindow = guiWindowMap[Hwnd(hWnd)] ?: creatingGuiWindow ?: error("gui window not found")
    info("wndProcGui ${guiWindow.windowName} umsg $uMsg")
    when(uMsg.toInt()){
        WM_SIZE -> {
            info("gui window size changing to ${Hwnd(hWnd).rect.str()}")
            guiWindow.onSize()
        }
        WM_GETMINMAXINFO -> {
            val ptr = lParam.toCPointer<MINMAXINFO>()
            ptr?.pointed?.apply {
                guiWindow.let {
                    ptMinTrackSize.x = it.minW
                    ptMinTrackSize.y = it.minH
                }
            } ?: warning("gui window min size set failed")
        }
        WM_COMMAND -> {
            fun LOWORD(value: Int) = value and 0xFFFF
            fun HIWORD(value: Int) = (value shr 16) and 0xFFFF
            val id = LOWORD(wParam.toInt()).toUShort()
            val notificationCode = HIWORD(wParam.toInt())
            guiWindow.onWmCommand(id,notificationCode)
        }
        WM_CLOSE -> guiWindow.onClose()
        WM_DESTROY -> guiWindow.onDestroy()
        else -> return DefWindowProcW(hWnd, uMsg, wParam, lParam)
    }
    return 0
}

private val guiWindowMap = mutableMapOf<Hwnd,GuiWindow>()
private var creatingGuiWindow :GuiWindow? = null
const val guiWindowClass = "gui_window"

interface GuiItem {
    val hwnd: Hwnd
    fun move(x:Int, y:Int, w:Int, h:Int) = hwnd.setRect(x, y, w, h)
    fun moveRect(rect: tagRECT) = hwnd.setRect(rect)
    val relativeRect get() = hwnd.rect.apply { toOrigin() }
}



@OptIn(ExperimentalForeignApi::class)
abstract class GuiWindow (
    val windowName:String,
    internal var minW:Int = 0, internal var minH:Int = 0,
    val parent:GuiWindow? = null
):GuiItem {
    //防止子类中访问未初始化的hwnd
    var onSize:()->Unit = { warning("onSize invoked default implement") }
        private set
    protected abstract fun onSize()

    var idIncrease = 100.toUShort()
        get() = field++
    val onClicks = mutableMapOf<UShort,()->Unit>()
    fun onWmCommand(id:UShort,nCode:Int){
        when(nCode){
            BN_CLICKED -> {
                onClicks[id]?.invoke() ?: error("no onClick found for id")
            }
        }
    }

    open fun onClose() {}
    open fun onDestroy() {}


    class Button(override val hwnd:Hwnd,val id:UShort):GuiItem

    @OptIn(ExperimentalForeignApi::class)
    fun button(string:String = "Click Me!", onClick:()->Unit):Button {
        val id = idIncrease
        val hwnd = CreateWindowExW(
            0u,
            "BUTTON",
            string,
            (WS_TABSTOP or WS_VISIBLE or WS_CHILD or BS_DEFPUSHBUTTON).toUInt(),
            100, 50, 0,0,
            hwnd.HWND,
            id.toLong().toCPointer(),
            GetModuleHandleW(null),
            null
        ).let { Hwnd(it) }
        onClicks[id] = onClick
        return Button(hwnd, id)
    }
    override val hwnd:Hwnd = run {
        val styleEx = (WS_EX_TOPMOST).toUInt()
        val style = (WS_OVERLAPPEDWINDOW or WS_VISIBLE).toUInt()
        if(parent == null){
            allocRECT {
                left = 0
                top = 0
                right = minW
                bottom = minH
                AdjustWindowRectEx(ptr,style,FALSE,styleEx)
                minW = right - left
                minH = bottom - top
            }
        }
        creatingGuiWindow = this
        val wnd = if (parent == null) CreateWindowExW(
            styleEx,guiWindowClass, windowName, style,
            CW_USEDEFAULT, CW_USEDEFAULT, 50, 50,
            null, null, GetModuleHandleW(null), null
        ) else CreateWindowExW(
            0u, guiWindowClass, windowName, (WS_CHILD or WS_VISIBLE).toUInt(),
            0, 0, 50, 50, parent.hwnd.HWND, (parent.idIncrease).toLong().toCPointer(),
            GetModuleHandleW(null), null
        )
        creatingGuiWindow = null
        Hwnd(wnd ?: error("gui window create failed ${GetLastError()}")).also{
            guiWindowMap[it] = this
        }
    }
    init {
        onSize = ::onSize
    }
}