package dsl

import dsl.MutStateList.Listener
import error.wrapExceptionName
import logger.warning
import wrapper.Destroyable
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> stateOf(value:T) = State(value)
fun <T> stateNull() = State<T?>(null)
fun <T> mutStateOf(value:T,constraint:((T)->T)? = null) = MutState(value,constraint)
fun <T: Any> mutStateNull(constraint:((T?)->T?)? = null) = MutState(null,constraint)
fun <T> mutStateList(vararg values:T) = MutStateList(*values)

fun <T> MutState.Scope.mutStateOf(value:T,trigger:Boolean = false,constraint:((T)->T)? = null,initListener:(T)->Unit)
= MutState(value,constraint).apply { listen(trigger,initListener) }

fun <T: Any> MutState.Scope.mutStateNull(trigger:Boolean = false, constraint:((T?)->T?)? = null, initListener:(T?)->Unit) = mutStateOf(null,trigger,constraint, initListener)

open class State<T>(open val value: T):ReadOnlyProperty<Any?,T>{
    override fun getValue(thisRef: Any?, property: KProperty<*>) = value
}
private var currentCombination: MutState.Scope.Combination? = null
class MutState<T>(value:T,val constraint:((T)->T)? = null):State<T>(value),ReadWriteProperty<Any?,T>{
    val listeners = mutableListOf<(T)->Unit>()
    override var value: T = value
        set(value) {
            if(field != value) {
                val newField = constraint?.invoke(value) ?: value
                if(field != newField){
                    field = newField
                    while (true){
                        try {
                            listeners.forEach { it(field) }
                        } catch (_: ConcurrentModificationException){
                            continue
                        }
                        break
                    }
                }
            }
        }
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return currentCombination?.run { track } ?: value
    }

    override fun toString(): String {
        return super.toString() +'{' + value.toString() +'}'
    }
    class SimpleScope:Scope{ override val _onDestroy = mutableListOf<()->Unit>() }
    interface Scope :Destroyable {
        fun <T> MutState<T>.listen(trigger:Boolean = false, listener:(T)->Unit){
            listeners += listener
            _onDestroy += { if (!listeners.remove(listener)) error("listener already removed") }
            if (trigger) listener(value)
        }
        fun <T> MutStateList<T>.listen(trigger:Boolean = false, listener: Listener<T>) {
            listeners += listener
            _onDestroy += { if (!listeners.remove(listener)) error("listener already removed") }
            if (trigger) {
                listener.onAnyChange()
                forEach { listener.onAdd(it) }
            }
        }
        fun <T,V> MutStateList<T>.generateState(func:(List<T>)->V) = MutState(func(delegate)).also {
            listen { it.value = func(delegate) }
        }

        @Gui
        class Combination {
            private val _trackedStates = mutableSetOf<MutState<*>>()
            val <T> MutState<T>.track:T get() = wrapExceptionName("Combination.track") {
                _trackedStates += this
                return value
            }
            val trackedStates:Set<MutState<*>> get() = _trackedStates
        }

        fun <T> extract(func:()->T): MutState<T> = wrapExceptionName("extract failed"){
            val combination = Combination()
            currentCombination = combination
            func()
            currentCombination = null
            require(combination.trackedStates.size == 1)
            combination.trackedStates.first() as MutState<T>
        }

        //the `func` will be invoked multiple times  so do not create new state in it.
        fun <T> combine(func:Combination.()->T):MutState<T> = wrapExceptionName("combine failed"){
            val firstCombination = Combination()
            currentCombination = firstCombination
            val initValue = firstCombination.func()
            currentCombination = null
            val state = mutStateOf(initValue)
            var trackedStates = firstCombination.trackedStates
            fun MutState<*>.listen1(listener:(Any?)->Unit){ listeners += listener }
            fun MutState<*>.remove1(listener:(Any?)->Unit){ if(!listeners.remove(listener)) error("combination listener already removed") }

            val update = object: Function1<Any?,Unit> {
                override operator fun invoke(any: Any?)= wrapExceptionName("combination update failed"){
                    val combination = Combination()
                    currentCombination = combination
                    val value = combination.func()
                    currentCombination = null
                    state.value = value
                    val newStates = combination.trackedStates
                    if(!trackedStates.containsAll(newStates)){
                        val added = newStates.subtract(trackedStates)
                        val removed = trackedStates.subtract(newStates)
                        trackedStates = newStates
                        removed.forEach { it.remove1(this) }
                        added.forEach { it.listen1(this) }
                    }
                }
            }
            trackedStates.forEach{ it.listen1(update) }
            _onDestroy += { trackedStates.forEach { it.remove1(update) } }
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