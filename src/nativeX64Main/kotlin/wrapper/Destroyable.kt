package wrapper

interface Destroyable {
    val _onDestroy:MutableList<()->Unit>
    fun addDestroy(block:()->Unit) = _onDestroy.plusAssign(block)
    fun destroy(){
        _onDestroy.forEach { it() }
        _onDestroy.clear()
    }
}