package wrapper

import dsl.Alignment
import dsl.Modifier
import dsl.State
import error.catchInKotlin
import error.wrapExceptionName
import geometry.Color
import kotlinx.cinterop.*
import logger.info
import logger.warning
import platform.windows.*
import kotlin.math.roundToInt
import kotlin.math.roundToLong


@OptIn(ExperimentalForeignApi::class)
fun wndProcGui(hWnd: HWND?, uMsg: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT = catchInKotlin("wndProcGui error") {
    val hwnd = Hwnd(hWnd)
    val guiWindowFromMap = guiWindowMap[hwnd]
    val guiWindow = guiWindowFromMap ?: creatingGuiWindow ?: error("gui window not found")
    val initialized = guiWindowFromMap != null
    fun default() = DefWindowProcW(hWnd, uMsg, wParam, lParam)
    fun LOWORD(value: Int) = value and 0xFFFF
    fun HIWORD(value: Int) = (value shr 16) and 0xFFFF
    when(uMsg.toInt()){
        WM_SIZE -> memScoped {
//            info("gui window size changing to ${hwnd.useRect { it.str() }}")
            if(!initialized) return default()
            wrapExceptionName({ "onSize error" }){
                guiWindow.onSize()
            }
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
        WM_HSCROLL -> {
            if(lParam != 0L) {
                guiWindow.onHScroll(Hwnd(lParam.toCPointer()))
            }
        }
        WM_VSCROLL -> memScoped {
            if(lParam != 0L) return@memScoped default()
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
        WM_ERASEBKGND -> {
            val hdc = wParam.toLong().toCPointer<HDC__>()
            if(!guiWindow.onEraseBkGnd(hdc)) return default()
        }
        WM_CLOSE -> if(!guiWindow.onClose()) return default()
        WM_DESTROY -> if(!guiWindow.onDestroy()) return default()
        WM_NCDESTROY -> {
            return default().apply {
                if(guiWindowMap.remove(Hwnd(hWnd)) == null) error("guiWindow ${Hwnd(hWnd)} remove failed")
            }
        }
        WM_DROPFILES -> {
            val path = memScoped {
                val hDrop : HDROP? = wParam.toLong().toCPointer()
                val buffer = allocArray<UShortVar>(MAX_PATH)
                val fileCount = DragQueryFileA(hDrop, 0xFFFFFFFFu, null, 0u)

                if (fileCount > 0u) {
                    DragQueryFileW(hDrop, 0u, buffer, MAX_PATH.toUInt())
                }
                DragFinish(hDrop)
                buffer.toKStringFromUtf16().ifBlank { return default() }
            }
            if(!guiWindow.onDropFile(path)) return default()
        }
        else -> return default()
    }
    return 0
}

val guiWindowMap = mutableMapOf<Hwnd, WindowProcess>()
private var creatingGuiWindow :GuiWindow? = null
const val guiWindowClass = "gui_window"
const val BN_LAST = 0x0FFF
const val MY_DESTROY = BN_LAST + 1

interface WindowProcess{
    fun onSize()
    fun onWmCommand(id:UShort,nCode:Int)
    fun onClose(): Boolean
    fun onDestroy(): Boolean
    fun onHScroll(hwnd: Hwnd)
    @OptIn(ExperimentalForeignApi::class)
    fun onEraseBkGnd(hdc:HDC?) = false
    fun onDropFile(path:String) = false
    val minW:Int
    val minH:Int
    val hwnd: Hwnd
    val parent: WindowProcess?
    val idIncrease: UShort
    val onCommand:  MutableMap<UShort, () -> Unit>

    @OptIn(ExperimentalForeignApi::class)
    private fun createSubHwnd(style:Int, className:String, windowName:String, onCommand:()->Unit = {}) = CreateWindowExW(
        0u,
        className,
        windowName,
        (WS_VISIBLE or WS_CHILD or style).toUInt(),
        0,0,0,0,
        hwnd.HWND,
        idIncrease.toLong().toCPointer(),
        GetModuleHandleW(null),
        null
    ).let { Hwnd(it ?: error("sub hwnd create failed ${GetLastError()} class $className window $windowName parent $hwnd")) }.apply {
        this@WindowProcess.onCommand[controlId] = onCommand
    }

    fun button(string:String, onClick:()->Unit) =
        createSubHwnd(WS_TABSTOP or BS_DEFPUSHBUTTON, WC_BUTTONA,string,onClick)

    fun edit(string:String, onEdit:(String)->Unit) =
        createSubHwnd(WS_TABSTOP or ES_LEFT or WS_BORDER, WC_EDITA,string).also {
            onCommand[it.controlId] = { onEdit(it.name) }
            onEdit(string)
        }

    fun text(text:String,alignment: Alignment) = createSubHwnd(alignment.staticStyle, WC_STATICA,text)

    @OptIn(ExperimentalForeignApi::class)
    fun <T> trackBar(range: ClosedRange<T>, steps:Int = 1000, onChange:(T)->Unit) where T:Number, T:Comparable<T>
            = createSubHwnd(0,"msctls_trackbar32","trackBar").apply {
        SendMessage!!(HWND, TBM_SETRANGEMIN.toUInt(), FALSE.toULong(), 0)
        SendMessage!!(HWND, TBM_SETRANGEMAX.toUInt(), FALSE.toULong(), steps.toLong())
        SendMessage!!(HWND, TBM_SETPAGESIZE.toUInt(), 0u, steps.toLong() / 10)
        onCommand[controlId] = {
            val pos = SendMessage!!(HWND,TBM_GETPOS.toUInt(),0u,0L)
            val start = range.start.toDouble()
            val end = range.endInclusive.toDouble()
            val progress = pos.toDouble() / steps.toDouble()
            val value = start + (end - start) * progress
            onChange(when(range.start::class){
                Int::class -> value.roundToInt() as T
                Float::class -> value.toFloat() as T
                Double::class -> value as T
                Byte::class -> value.roundToInt().toByte() as T
                Long::class -> value.roundToLong() as T
                else -> error("unsupported trackbar type: ${range.start::class}")
            })
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
abstract class GuiWindow (
    val windowName:String,
    modifier: Modifier,
    style: Int = 0,
    final override val parent: WindowProcess? = null
): WindowProcess {
    override var minW = modifier._minW
    override var minH = modifier._minH
    override fun onSize(){
        if(scrollableHeight == -1) return
        hwnd.updateScrollSize(SB_VERT,scrollableHeight)
    }
    abstract val scrollableHeight: Int
    abstract val scrollableWidth: Int

    override var idIncrease = 0.toUShort()
        get() {
            do { field++ } while (onCommand.containsKey(field))
            return field
        }
    override val onCommand = mutableMapOf<UShort,()->Unit>()
    override fun onWmCommand(id:UShort, nCode:Int){
        when(nCode){
            BN_CLICKED -> {
                onCommand[id]?.invoke() ?: error("no onClick found for id $id in $hwnd")
            }
            EN_CHANGE -> {
                onCommand[id]?.invoke()// ?: warning("no onEdit found for id $id in $hwnd")
            }
            MY_DESTROY -> {
                onCommand.remove(id)
            }
        }
    }

    override fun onClose() = false
    override fun onDestroy(): Boolean{
        return false
    }
    override fun onHScroll(hwnd: Hwnd){
        onCommand[hwnd.controlId]?.invoke()
    }

    override val hwnd:Hwnd = run {
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

                left = 0
                top = 0
                right = modifier.width
                bottom = modifier.height
                AdjustWindowRectEx(ptr,style,FALSE,styleEx)

                CreateWindowExW(
                    styleEx,guiWindowClass, windowName, style,
                    CW_USEDEFAULT, CW_USEDEFAULT, right - left,bottom - top,
                    null, null, GetModuleHandleW(null), null
                ) ?: error("gui top window create failed ${GetLastError()} window $windowName")
            }
        } else CreateWindowExW(
            0u, guiWindowClass, windowName, (WS_CHILD or WS_VISIBLE or style).toUInt(),
            0, 0, 0, 0, parent.hwnd.HWND, (parent.idIncrease).toLong().toCPointer(),
            GetModuleHandleW(null), null
        ) ?: error("gui child window create failed ${GetLastError()} window $windowName")
        creatingGuiWindow = null
        Hwnd(wnd).also{
            guiWindowMap[it] = this
        }
    }
}