package dsl

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MutableState<T>(value:T):ReadWriteProperty<Any?,T>{
    private val listeners = mutableListOf<(T)->Unit>()
    var value = value
        set(value) {
            field = value
            listeners.forEach { it(value) }
        }
    fun listen(listener:(T)->Unit) = listeners.plusAssign(listener)
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}