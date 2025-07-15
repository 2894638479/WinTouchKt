package wrapper

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.ClassSerialDescriptorBuilder
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.serializer

abstract class SerializerWrapper<T,D:SerializerWrapper.Descriptor<T>>(name:String,val desc:D):KSerializer<T> {
    final override val descriptor = buildClassSerialDescriptor(name){
        desc.items.forEach {
            it.run { buildElement() }
        }
    }
    abstract class Descriptor<T>{
        interface Item <T,E:Any>{
            fun ClassSerialDescriptorBuilder.buildElement()
            fun CompositeDecoder.deserialize(descriptor:SerialDescriptor,index:Int):E
            fun CompositeEncoder.serialize(descriptor: SerialDescriptor,index:Int,value:T)
        }
        inline infix fun <reified E:Any> String.from(crossinline serializeValue:T.()->E?) = create(this,serializeValue)
        inline fun <reified E:Any> create(name:String, crossinline serializeValue:T.()->E?) = object :Item <T,E>{
            override fun ClassSerialDescriptorBuilder.buildElement() {
                element<E>(name)
            }
            override fun CompositeDecoder.deserialize(descriptor:SerialDescriptor, index:Int)
                    = decodeSerializableElement(descriptor,index,serializer<E>())
            override fun CompositeEncoder.serialize(descriptor: SerialDescriptor, index:Int, value:T){
                serializeValue(value)?.let {
                    encodeSerializableElement(descriptor,index, serializer<E>(),it)
                }
            }
        }
        abstract val items: List<Item<T,*>>
    }

    abstract class DeserializeScope<T,D:Descriptor<T>>(private val decoder: CompositeDecoder,private val wrapper: SerializerWrapper<T,D>){
        internal val tasks = mutableMapOf<Descriptor.Item<T, *>,()->Unit>()
        infix fun <E:Any> Descriptor.Item<T, E>.to(task:(E)->Unit){
            tasks[this] = { task(deserialize()) }
        }
        abstract fun end():T
        fun <E:Any> Descriptor.Item<T,E>.deserialize():E{
            return decoder.deserialize(wrapper.descriptor,wrapper.desc.items.indexOf(this))
        }
    }
    abstract fun deserializeScope(decoder: CompositeDecoder):DeserializeScope<T,D>

    final override fun deserialize(decoder: Decoder): T {
        var scope:DeserializeScope<T,D>? = null
        decoder.decodeStructure(descriptor){
            val scope = deserializeScope(this).apply { scope = this }
            while (true){
                val index = decodeElementIndex(descriptor)
                if(index == CompositeDecoder.DECODE_DONE) break
                val item = desc.items.getOrNull(index) ?: error("unexpected index $index")
                scope.tasks[item]?.invoke() ?: error("not found task")
            }
        }
        return scope?.end() ?: error("deserialize scope is null")
    }

    final override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeStructure(descriptor){
            desc.items.forEachIndexed { index,it ->
                it.run { serialize(descriptor,index,value) }
            }
        }
    }
}