package wrapper

import error.errorBox
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@OptIn(ExperimentalNativeApi::class)
value class WeakRef <T : Any> private constructor (private val ref:WeakReference<T>){
    constructor(referred:T):this(WeakReference(referred))
    fun get() = ref.get()
    val value get() = get()
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = get()
}

@OptIn(ExperimentalNativeApi::class)
value class WeakRefNonNull <T : Any> private constructor (private val ref:WeakReference<T>){
    constructor(referred:T):this(WeakReference(referred))
    fun get() = ref.get() ?: errorBox("weak ref already inactive")
    val value get() = get()
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = get()
}

class WeakRefDel<T:Any>(referred: T? = null): ReadWriteProperty<Any?, T?> {
    @OptIn(ExperimentalNativeApi::class)
    private var ref = referred?.let { WeakReference(it) }
    @OptIn(ExperimentalNativeApi::class)
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return ref?.value
    }
    @OptIn(ExperimentalNativeApi::class)
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        ref = value?.let { WeakReference(it) }
    }
}