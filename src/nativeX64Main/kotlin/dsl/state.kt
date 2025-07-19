package dsl

import dsl.MutStateList.Listener
import wrapper.Destroyable
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> stateOf(value:T) = State(value)
fun <T> stateNull() = State<T?>(null)
fun <T> mutStateOf(value:T,initListener:((T)->Unit)? = null) = MutState(value,initListener)
fun <T> mutStateNull(initListener:((T?)->Unit)? = null) = MutState<T?>(null,initListener)
fun <T> mutStateList(vararg values:T) = MutStateList(*values)

open class State<T>(open val value: T):ReadOnlyProperty<Any?,T>{
    override fun getValue(thisRef: Any?, property: KProperty<*>) = value
}

class MutState<T>(value:T,initListener:((T)->Unit)? = null):State<T>(value),ReadWriteProperty<Any?,T>{
    private val listeners = mutableListOf<(T)->Unit>().also {
        if(initListener != null) it += initListener
    }
    override var value = value
        set(value) {
            if(field != value) {
                field = value
                listeners.forEach { it(value) }
            }
        }
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
    class SimpleScope:Scope{ override val _onDestroy = mutableListOf<()->Unit>() }
    interface Scope :Destroyable {
        fun <T> MutState<T>.listen(trigger:Boolean = false, listener:(T)->Unit){
            if (trigger) listener(value)
            listeners += listener
            _onDestroy += { if (!listeners.remove(listener)) error("listener already removed") }
        }
        fun <T> MutStateList<T>.listen(trigger:Boolean = false, listener: Listener<T>) {
            if (trigger) listener.onAnyChange()
            listeners += listener
            _onDestroy += { if (!listeners.remove(listener)) error("listener already removed") }
        }
        fun <T,V> MutStateList<T>.generateState(func:(List<T>)->V) = MutState(func(delegate)).also {
            listen { it.value = func(delegate) }
        }

        class Combination {
            private val _trackedStates = mutableSetOf<MutState<*>>()
            val <T> MutState<T>.tracked:T get() {
                _trackedStates += this
                return value
            }
            val trackedStates get() = _trackedStates
        }

        //the `func` will be invoked multiple times  so do not create new state in it.
        fun <T> combine(func:Combination.()->T):MutState<T>{
            val firstCombination = Combination()
            val initValue = firstCombination.func()
            val state = mutStateOf(initValue)
            var trackedStates = firstCombination.trackedStates.toMutableSet()
            fun <T> MutState<T>.listen1(listener:(T)->Unit){ listeners += listener }
            fun <T> MutState<T>.remove1(listener:(T)->Unit){ if(!listeners.remove(listener)) error("combination listener already removed") }
            fun update(any: Any?){
                val combination = Combination()
                state.value = combination.func()
                val newStates = combination.trackedStates
                if(!trackedStates.containsAll(newStates)){
                    val added = newStates.subtract(trackedStates)
                    val removed = trackedStates.subtract(newStates)
                    trackedStates = newStates
                    added.forEach { it.listen1(::update) }
                    removed.forEach { it.remove1(::update) }
                }
            }
            trackedStates.forEach{ it.listen1(::update) }
            _onDestroy += { trackedStates.forEach { it.remove1(::update) } }
            return state
        }
    }
}

class MutStateList<T> private constructor(internal val delegate:MutableList<T>):MutableList<T> by delegate {
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