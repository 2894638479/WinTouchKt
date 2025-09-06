package wrapper

import kotlinx.cinterop.*
import libs.Clib.hwndHolder
import logger.info
import logger.warning
import platform.windows.*
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalForeignApi::class)
value class Hwnd(val value:CPointer<hwndHolder>){
    constructor(hwnd: HWND?):this(hwnd?.reinterpret<hwndHolder>() ?: error("hwnd is null"))
    val HWND:HWND get() = value.reinterpret()
    fun <T> useRect(block:(RECT)->T) = memScoped {
        alloc<RECT>().run {
            GetClientRect(HWND,ptr)
            block(this)
        }
    }
    fun setRect(rect:RECT) = rect.run { setRect(left,top,right - left,bottom - top) }
    fun show() = ShowWindow(HWND, SW_SHOW)
    fun hide() = ShowWindow(HWND, SW_HIDE)
    val visible get() = IsWindowVisible(HWND) != FALSE
    fun update() = UpdateWindow(HWND)
    fun showAndUpdate() { show();update() }
    fun setRect(x:Int, y:Int, w:Int, h:Int){
//        info("hwnd $name moved to $x $y ,size $w $h")
        MoveWindow(HWND,x,y,w,h, TRUE).ifFalse { warning("hwnd move false") }
    }
    fun sendMessage(p1:UInt,p2:ULong,p3:Long) = SendMessage!!(HWND,p1,p2,p3)
    fun close() = CloseWindow(HWND)
    fun destroy() {
        parent?.let {
            SendMessage!!(
                it.HWND, WM_COMMAND.toUInt(),
                (MY_DESTROY.toULong() shl 16) or controlId.toULong(),
                HWND.toLong()
            )
        }
        DestroyWindow(HWND)
    }
    fun enable() = EnableWindow(HWND,TRUE)
    fun disable() = EnableWindow(HWND,FALSE)
    fun enable(bool: Boolean) = if(bool) enable() else disable()
    val nameLength get() = GetWindowTextLengthW(HWND)
    var sel get() = memScoped {
            val start = alloc<IntVar>()
            val end = alloc<IntVar>()
            SendMessage!!(HWND, EM_GETSEL.toUInt(), start.ptr.toLong().toULong(), end.ptr.toLong())
            start.value to end.value
        }
        set(value) {
            val len = nameLength
            val start = min(len,value.first)
            val end = min(len,value.second)
            SendMessage!!(HWND, EM_SETSEL.toUInt(),start.toULong(),end.toLong())
        }
    var name:String
        get() {
            val length = nameLength
            memScoped {
                val buffer = allocArray<UShortVar>(length + 1)
                GetWindowTextW(HWND,buffer,length + 1)
                return buffer.toKStringFromUtf16()
            }
        }
        set(value) {
            SetWindowTextW(HWND, value)
        }
    val controlId get() = GetDlgCtrlID(HWND).toUShort()
    val parent get() = GetParent(HWND)?.let{ Hwnd(it) }
    fun invalidateRect(rect:RECT? = null) = InvalidateRect(HWND,rect?.ptr,1)
    fun validateRect(rect: RECT? = null) = ValidateRect(HWND,rect?.ptr)
    inline fun scroll(bar:Int,value:tagSCROLLINFO.()->Int) = memScoped {
        val si = alloc<tagSCROLLINFO>()
        si.fMask = SIF_ALL.toUInt()
        GetScrollInfo(HWND,bar,si.ptr)
        si.fMask = SIF_POS.toUInt()
        val originPos = si.nPos
        si.nPos = si.value()
        SetScrollInfo(HWND,bar,si.ptr,TRUE)
        GetScrollInfo(HWND,bar,si.ptr)
        si.nPos - originPos
    }
    fun updateScrollSize(bar:Int,height:Int) = memScoped {
        val si = alloc<tagSCROLLINFO>()
        si.fMask = SIF_POS.toUInt()
        GetScrollInfo(HWND,bar,si.ptr)
        si.nPage = useRect { it.height.toUInt() }
        si.nMin = 0
        si.nMax = height - 1
        si.fMask = SIF_ALL.toUInt()
        si.nPos = min(si.nPos,si.nMax - si.nPage.toInt())
        si.nPos = max(si.nPos,0)
        SetScrollInfo(HWND,bar,si.ptr,TRUE)
        ScrollWindow(HWND,0,-si.nPos,null,null)
    }
    override fun toString() = "Hwnd{name=$name,rect=${useRect { it.str() }},parent:$parent}"
}