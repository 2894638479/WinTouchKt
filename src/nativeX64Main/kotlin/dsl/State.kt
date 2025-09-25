package dsl

import error.wrapExceptionName
import wrapper.Destroyable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T> stateOf(value:T) = State(value)
fun <T> stateNull() = State<T?>(null)
fun <T> mutStateOf(value:T,constraint:((T)->T)? = null) = MutState(value,constraint)
fun <T: Any> mutStateNull(constraint:((T?)->T?)? = null) = MutState(null,constraint)
fun <T> mutStateList(vararg values:T) = MutStateList(*values)

internal var currentCombination: State.Scope.Combination? = null
fun <T> withCombination(combination: State.Scope.Combination,block: State.Scope.Combination.()->T):T{
    val rem = currentCombination
    currentCombination = combination
    return combination.block().also { currentCombination = rem }
}

open class State<out T>(open val value: T):ReadOnlyProperty<Any?,T>{
    override fun getValue(thisRef: Any?, property: KProperty<*>) = value
    context(scope:Scope)
    fun listen(trigger:Boolean, listener:(T)->Unit){
        listen(listener)
        if(trigger) listener(value)
    }
    context(scope:Scope)
    open fun listen(listener:(T)->Unit){}

    interface Trackable<T> {
        val value:T
        val listeners: MutableList<(T)->Unit>
        val track:T get() {
            currentCombination?.track(this)
            return value
        }
    }
    interface Scope :Destroyable {
        @Gui
        class Combination {
            private val _trackedStates = mutableSetOf<Trackable<*>>()
            fun track(trackable: Trackable<*>){
                _trackedStates += trackable
            }
            val trackedStates:Set<Trackable<*>> get() = _trackedStates
        }

        fun <T> extract(func:()->T): MutState<T> = wrapExceptionName("extract failed"){
            val combination = Combination()
            withCombination(combination) { func() }
            require(combination.trackedStates.size == 1)
            combination.trackedStates.first() as MutState<T>
        }

        fun <T> combine(func:Combination.()->T): State<T> = mutCombine(func)
        //the `func` will be invoked multiple times  so do not create new state in it.
        fun <T> mutCombine(func:Combination.()->T): MutState<T> = wrapExceptionName("combine failed"){
            val firstCombination = Combination()
            val initValue = withCombination(firstCombination){ func() }
            val state = mutStateOf(initValue)
            var trackedStates = firstCombination.trackedStates
            fun Trackable<*>.listen1(listener:(Any?)->Unit){ listeners += listener }
            fun Trackable<*>.remove1(listener:(Any?)->Unit){ if(!listeners.remove(listener)) error("combination listener already removed") }

            val update = object: (Any?)->Unit {
                override operator fun invoke(any: Any?)= wrapExceptionName({ "combination update failed $trackedStates" }){
                    val combination = Combination()
                    val value = withCombination(combination){func()}
                    val newStates = combination.trackedStates
                    if(true){
                        val added = newStates.subtract(trackedStates)
                        val removed = trackedStates.subtract(newStates)
                        trackedStates = newStates
                        removed.forEach { it.remove1(this) }
                        added.forEach { it.listen1(this) }
                    }
                    state.value = value
                }
            }
            trackedStates.forEach{ it.listen1(update) }
            _onDestroy += { trackedStates.forEach { it.remove1(update) } }
            return state
        }
    }
}

