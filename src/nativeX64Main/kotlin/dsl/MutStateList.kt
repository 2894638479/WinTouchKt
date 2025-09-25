package dsl

import kotlin.reflect.KProperty

class MutStateList<T> private constructor(internal val delegate:MutableList<T>):
    MutableList<T> by delegate,
    State<List<T>>(delegate),
    State.Trackable<List<T>> {
    val list:List<T> get() = delegate
    constructor(vararg value:T):this(mutableListOf(*value))
    override val listeners = mutableListOf<(List<T>)->Unit>()
    internal val listListeners = mutableListOf<Listener<T>>(Listener{ list ->
        listeners.forEach { it(list) }
    })
    override fun getValue(thisRef: Any?, property: KProperty<*>) = track
    fun interface Listener<T>{
        fun onAnyChange(list:List<T>)
        fun onAdd(element:T){}
        fun onRemove(element: T){}
    }
    context(scope: State.Scope)
    fun listen(trigger:Boolean = false, listener: Listener<T>) {
        listListeners += listener
        scope._onDestroy += { if (!listListeners.remove(listener)) error("listener already removed") }
        if (trigger) {
            listener.onAnyChange(this)
            forEach { listener.onAdd(it) }
        }
    }
    context(scope: State.Scope)
    fun <V> state(func:(List<T>)->V) = MutState(func(delegate)).also { state ->
        listen { state.value = func(delegate) }
    }
    override fun add(element: T) = delegate.add(element).apply {
        listListeners.forEach {
            it.onAdd(element)
            it.onAnyChange(this@MutStateList)
        }
    }
    override fun add(index: Int, element: T) = delegate.add(index, element).apply {
        listListeners.forEach {
            it.onAdd(element)
            it.onAnyChange(this@MutStateList)
        }
    }
    override fun addAll(elements: Collection<T>) = delegate.addAll(elements).apply {
        listListeners.forEach {
            for(element in elements) { it.onAdd(element) }
            it.onAnyChange(this@MutStateList)
        }
    }
    override fun addAll(index: Int, elements: Collection<T>) = delegate.addAll(index, elements).apply {
        listListeners.forEach {
            for(element in elements) { it.onAdd(element) }
            it.onAnyChange(this@MutStateList)
        }
    }
    override fun clear() {
        val remembered = delegate.toList()
        delegate.clear().apply {
            listListeners.forEach {
                for(element in remembered) { it.onRemove(element) }
                it.onAnyChange(this@MutStateList)
            }
        }
    }
    override fun removeAt(index: Int) = delegate.removeAt(index).apply {
        listListeners.forEach {
            it.onRemove(this)
            it.onAnyChange(this@MutStateList)
        }
    }
    override fun remove(element: T) = delegate.remove(element).apply {
        if(this) listListeners.forEach {
            it.onRemove(element)
            it.onAnyChange(this@MutStateList)
        }
    }
    override fun removeAll(elements: Collection<T>):Boolean {
        var modified = false
        for(element in elements) {
            if (delegate.remove(element)) {
                listListeners.forEach { it.onRemove(element) }
                modified = true
            }
        }
        if (modified) listListeners.forEach { it.onAnyChange(this) }
        return modified
    }
}