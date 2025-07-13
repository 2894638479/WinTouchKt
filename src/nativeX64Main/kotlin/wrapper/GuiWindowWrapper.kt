package wrapper

import dsl.Alignment
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
    fun default() = DefWindowProcW(hWnd, uMsg, wParam, lParam)
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
        WM_CLOSE -> if(!guiWindow.onClose()) return default()
        WM_DESTROY -> if(!guiWindow.onDestroy()) return default()
        else -> return default()
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
    val onCommand = mutableMapOf<UShort,()->Unit>()
    fun onWmCommand(id:UShort,nCode:Int){
        when(nCode){
            BN_CLICKED -> {
                onCommand[id]?.invoke() ?: error("no onClick found for id $id")
            }
            EN_CHANGE -> {
                onCommand[id]?.invoke() ?: warning("no onEdit found for id $id")
            }
        }
    }

    open fun onClose() = false
    open fun onDestroy() = false

    private fun createSubHwnd(style:Int,className:String,windowName:String) = CreateWindowExW(
        0u,
        className,
        windowName,
        (WS_VISIBLE or WS_CHILD or style).toUInt(),
        100, 100, 100,100,
        hwnd.HWND,
        idIncrease.toLong().toCPointer(),
        GetModuleHandleW(null),
        null
    ).let { Hwnd(it ?: error("sub hwnd create failed ${GetLastError()}")) }

    fun button(string:String, onClick:()->Unit) = createSubHwnd( WS_TABSTOP or BS_DEFPUSHBUTTON, WC_BUTTONA,string).apply {
        onCommand[controlId] = onClick
    }

    fun edit(string:String, onEdit:(String)->Unit) = createSubHwnd(WS_TABSTOP or ES_LEFT or WS_BORDER, WC_EDITA,string).also {
        onEdit(string)
        onCommand[it.controlId] = {
            onEdit(it.name)
        }
    }

    fun text(text:String,alignment: Alignment) = createSubHwnd(alignment.staticStyle, WC_STATICA,text)

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