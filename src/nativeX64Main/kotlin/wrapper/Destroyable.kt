package wrapper

interface Destroyable {
    val _onDestroy:MutableList<()->Unit>
    fun destroy(){
        _onDestroy.forEach { it() }
        _onDestroy.clear()
    }
}