package wrapper

import dsl.Alignment
import error.catchInKotlin
import kotlinx.cinterop.*
import logger.info
import logger.warning
import platform.windows.*


@OptIn(ExperimentalForeignApi::class)
fun wndProcGui(hWnd: HWND?, uMsg: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT = catchInKotlin {
    val hwnd = Hwnd(hWnd)
    val guiWindowFromMap = guiWindowMap[hwnd]
    val guiWindow = guiWindowFromMap ?: creatingGuiWindow ?: error("gui window not found")
    val initialized = guiWindowFromMap != null
    info("wndProcGui ${guiWindow.windowName} umsg $uMsg")
    fun default() = DefWindowProcW(hWnd, uMsg, wParam, lParam)
    fun LOWORD(value: Int) = value and 0xFFFF
    fun HIWORD(value: Int) = (value shr 16) and 0xFFFF
    when(uMsg.toInt()){
        WM_SIZE -> memScoped {
            info("gui window size changing to ${hwnd.rect.str()}")
            if(!initialized) return default()
            guiWindow.onSize()
            if(guiWindow.scrollableHeight == -1) return default()
            hwnd.updateScrollSize(SB_VERT,guiWindow.scrollableHeight)
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
            val id = LOWORD(wParam.toInt()).toUShort()
            val notificationCode = HIWORD(wParam.toInt())
            guiWindow.onWmCommand(id,notificationCode)
        }
        WM_VSCROLL -> memScoped {
            val dy = hwnd.scroll(SB_VERT) {
                when (LOWORD(wParam.toInt())) {
                    SB_TOP -> nMin
                    SB_BOTTOM -> nMax
                    SB_LINEUP -> nPos - 10
                    SB_LINEDOWN -> nPos + 10
                    SB_PAGEUP -> nPos - nPage.toInt()
                    SB_PAGEDOWN -> nPos + nPage.toInt()
                    SB_THUMBTRACK, SB_THUMBPOSITION -> HIWORD(wParam.toInt())
                    else -> nPos
                }
            }
            ScrollWindow(hWnd,0,-dy,null,null)
        }
        WM_CLOSE -> if(!guiWindow.onClose()) return default()
        WM_DESTROY -> if(!guiWindow.onDestroy()) return default()
        WM_NCDESTROY -> {
            return default().apply {
                guiWindowMap.remove(Hwnd(hWnd))
            }
        }
        else -> return default()
    }
    return 0
}

private val guiWindowMap = mutableMapOf<Hwnd,GuiWindow>()
private var creatingGuiWindow :GuiWindow? = null
const val guiWindowClass = "gui_window"

@OptIn(ExperimentalForeignApi::class)
abstract class GuiWindow (
    val windowName:String,
    internal var minW:Int = 0, internal var minH:Int = 0,
    style: Int = 0,
    val parent:GuiWindow? = null
) {
    abstract fun onSize()
    abstract val scrollableHeight: Int
    abstract val scrollableWidth: Int

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

    fun button(string:String, onClick:()->Unit) = createSubHwnd(WS_TABSTOP or BS_DEFPUSHBUTTON, WC_BUTTONA,string).apply {
        onCommand[controlId] = onClick
    }

    fun edit(string:String, onEdit:(String)->Unit) = createSubHwnd(WS_TABSTOP or ES_LEFT or WS_BORDER, WC_EDITA,string).also {
        onEdit(string)
        onCommand[it.controlId] = {
            onEdit(it.name)
        }
    }

    fun text(text:String,alignment: Alignment) = createSubHwnd(alignment.staticStyle, WC_STATICA,text)

    val hwnd:Hwnd = run {
        val styleEx = (WS_EX_TOPMOST).toUInt()
        creatingGuiWindow = this
        val wnd =  if(parent == null){
            val style = (WS_OVERLAPPEDWINDOW or WS_VISIBLE or style).toUInt()
            allocRECT {
                left = 0
                top = 0
                right = minW
                bottom = minH
                AdjustWindowRectEx(ptr,style,FALSE,styleEx)
                minW = right - left
                minH = bottom - top
            }
            CreateWindowExW(
                styleEx,guiWindowClass, windowName, style,
                CW_USEDEFAULT, CW_USEDEFAULT, 50, 50,
                null, null, GetModuleHandleW(null), null
            )
        } else CreateWindowExW(
            0u, guiWindowClass, windowName, (WS_CHILD or WS_VISIBLE or style).toUInt(),
            0, 0, 50, 50, parent.hwnd.HWND, (parent.idIncrease).toLong().toCPointer(),
            GetModuleHandleW(null), null
        )
        creatingGuiWindow = null
        Hwnd(wnd ?: error("gui window create failed ${GetLastError()}")).also{
            guiWindowMap[it] = this
        }
    }
}