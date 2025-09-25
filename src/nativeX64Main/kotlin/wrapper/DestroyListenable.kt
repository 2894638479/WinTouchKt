package wrapper

interface Destroyable{
    fun destroy()
}

interface DestroyListenable: Destroyable {
    val _onDestroy:MutableList<()->Unit>
    override fun destroy(){
        _onDestroy.forEach { it() }
        _onDestroy.clear()
    }
}