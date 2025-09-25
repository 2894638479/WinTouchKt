package wrapper

import dsl.State
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Delegate<T>(val state: State<T>,val set:(T)->Unit):
    ReadWriteProperty<Any?,T>, ReadOnlyProperty<Any?,T> by state{
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) { set(value) }
}

class RODelegate<T>(val get:()->T): ReadOnlyProperty<Any?,T>{
    override fun getValue(thisRef: Any?, property: KProperty<*>) = get()
}