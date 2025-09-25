package dsl

import logger.warning
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MutState<T>(value:T,val constraint:((T)->T)? = null):State<T>(value), ReadWriteProperty<Any?, T>, State.Trackable<T> {
    override val listeners = mutableListOf<(T)->Unit>()
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
                            warning("concurrent modifying $this, retrying")
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
    override fun getValue(thisRef: Any?, property: KProperty<*>) = track

    context(scope: Scope)
    override fun listen(listener:(T)->Unit){
        listeners += listener
        scope._onDestroy += { if (!listeners.remove(listener)) error("listener already removed") }
    }

    override fun toString(): String {
        return super.toString() +'{' + value.toString() +'}'
    }
    class SimpleScope:Scope{ override val _onDestroy = mutableListOf<()->Unit>() }

}