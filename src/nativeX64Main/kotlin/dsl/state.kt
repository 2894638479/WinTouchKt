package dsl

import dsl.MutStateList.Listener
import wrapper.Destroyable
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> stateOf(value:T) = State(value)
fun <T> stateNull() = State<T?>(null)
fun <T> mutStateOf(value:T) = MutState(value)
fun <T> mutStateNull() = MutState<T?>(null)
fun <T> mutStateList(vararg values:T) = MutStateList(*values)

fun <T> MutState.Scope.mutStateOf(value:T,trigger:Boolean = false,initListener:(T)->Unit)
= MutState(value).apply { listen(trigger,initListener) }

fun <T> MutState.Scope.mutStateNull(trigger:Boolean = false,initListener:(T?)->Unit) = mutStateOf<T?>(null,trigger, initListener)

open class State<T>(open val value: T):ReadOnlyProperty<Any?,T>{
    override fun getValue(thisRef: Any?, property: KProperty<*>) = value
}

class MutState<T>(value:T):State<T>(value),ReadWriteProperty<Any?,T>{
    private val listeners = mutableListOf<(T)->Unit>()
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
            if (trigger) {
                listener.onAnyChange()
                forEach { listener.onAdd(it) }
            }
            listeners += listener
            _onDestroy += { if (!listeners.remove(listener)) error("listener already removed") }
        }
        fun <T,V> MutStateList<T>.generateState(func:(List<T>)->V) = MutState(func(delegate)).also {
            listen { it.value = func(delegate) }
        }

        @Gui
        class Combination {
            private val _trackedStates = mutableSetOf<MutState<*>>()
            val <T> MutState<T>.track:T get() {
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
    val list:List<T> get() = delegate
    constructor(vararg value:T):this(mutableListOf(*value))
    internal val listeners = mutableListOf<Listener<T>>()
    fun interface Listener<T>{
        fun onAnyChange()
        fun onAdd(element:T){}
        fun onRemove(element: T){}
    }
    override fun add(element: T) = delegate.add(element).apply {
        listeners.forEach {
            it.onAdd(element)
            it.onAnyChange()
        }
    }
    override fun add(index: Int, element: T) = delegate.add(index, element).apply {
        listeners.forEach {
            it.onAdd(element)
            it.onAnyChange()
        }
    }
    override fun addAll(elements: Collection<T>) = delegate.addAll(elements).apply {
        listeners.forEach {
            for(element in elements) { it.onAdd(element) }
            it.onAnyChange()
        }
    }
    override fun addAll(index: Int, elements: Collection<T>) = delegate.addAll(index, elements).apply {
        listeners.forEach {
            for(element in elements) { it.onAdd(element) }
            it.onAnyChange()
        }
    }
    override fun clear() {
        val remembered = delegate.toList()
        delegate.clear().apply {
            listeners.forEach {
                for(element in remembered) { it.onRemove(element) }
                it.onAnyChange()
            }
        }
    }
    override fun removeAt(index: Int) = delegate.removeAt(index).apply {
        listeners.forEach {
            it.onRemove(this)
            it.onAnyChange()
        }
    }
    override fun remove(element: T) = delegate.remove(element).apply {
        if(this) listeners.forEach {
            it.onRemove(element)
            it.onAnyChange()
        }
    }
    override fun removeAll(elements: Collection<T>):Boolean {
        var modified = false
        for(element in elements) {
            if (delegate.remove(element)) {
                listeners.forEach { it.onRemove(element) }
                modified = true
            }
        }
        if (modified) listeners.forEach { it.onAnyChange() }
        return modified
    }
}