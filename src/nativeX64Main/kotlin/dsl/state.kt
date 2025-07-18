package dsl

import dsl.StateList.Listener
import wrapper.Destroyable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty



class State<T>(value:T,mutable:Boolean = true):ReadWriteProperty<Any?,T>{
    private val listeners = if(mutable) mutableListOf<(T)->Unit>() else null
    val mutable get() = listeners != null
    var value = value
        set(value) {
            if(field != value) {
                field = value
                listeners?.forEach { it(value) }
            }
        }
    interface Scope :Destroyable {
        fun <T> State<T>.listen(trigger:Boolean = false,listener:(T)->Unit){
            if (trigger) listener(value)
            listeners?.let {
                it += listener
                _onDestroy += { if (!listeners.remove(listener)) error("listener already removed") }
            }
        }
        fun <T> StateList<T>.listen(trigger:Boolean = false,listener: Listener<T>) = listener.also {
            if (trigger) listener.onAnyChange()
            listeners += it
            _onDestroy += { if (!listeners.remove(listener)) error("listener already removed") }
        }
        fun <T,V> StateList<T>.generateState(func:(List<T>)->V) = State(func(delegate)).also {
            listen { it.value = func(delegate) }
        }
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

class StateList<T> private constructor(internal val delegate:MutableList<T>):MutableList<T> by delegate {
    constructor(vararg value:T):this(mutableListOf(*value))
    internal val listeners = mutableListOf<Listener<T>>()
    fun interface Listener<T>{
        fun onAnyChange()
        fun onAddOne(element:T){}
        fun onRemoveOne(element: T){}
        fun onOtherChange(){}
    }
    override fun add(element: T) = delegate.add(element).apply {
        listeners.forEach {
            it.onAddOne(element)
            it.onAnyChange()
        }
    }
    override fun add(index: Int, element: T) = delegate.add(index, element).apply {
        listeners.forEach {
            it.onAddOne(element)
            it.onAnyChange()
        }
    }
    override fun addAll(elements: Collection<T>) = delegate.addAll(elements).apply {
        listeners.forEach {
            it.onOtherChange()
            it.onAnyChange()
        }
    }
    override fun addAll(index: Int, elements: Collection<T>) = delegate.addAll(index, elements).apply {
        listeners.forEach {
            it.onOtherChange()
            it.onAnyChange()
        }
    }
    override fun clear() = delegate.clear().apply {
        listeners.forEach {
            it.onOtherChange()
            it.onAnyChange()
        }
    }
    override fun removeAt(index: Int) = delegate.removeAt(index).apply {
        listeners.forEach {
            it.onRemoveOne(this)
            it.onAnyChange()
        }
    }
    override fun remove(element: T) = delegate.remove(element).apply {
        if(this) listeners.forEach {
            it.onRemoveOne(element)
            it.onAnyChange()
        }
    }
    override fun removeAll(elements: Collection<T>) = delegate.removeAll(elements).apply {
        if(this) listeners.forEach {
            it.onOtherChange()
            it.onAnyChange()
        }
    }
}