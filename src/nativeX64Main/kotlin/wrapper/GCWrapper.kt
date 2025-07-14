package wrapper

import logger.info
import kotlin.native.runtime.GC
import kotlin.native.runtime.GCInfo
import kotlin.native.runtime.MemoryUsage
import kotlin.native.runtime.NativeRuntimeApi

@OptIn(NativeRuntimeApi::class)
fun printGCInfo(info: GCInfo){
    fun  Map<String, MemoryUsage>.string() = entries.joinToString(",","{","}") {
        "${it.key}: ${it.value.totalObjectsSizeBytes} bytes" }
    info("gc info: \n" +
            "passed time:${info.run { endTimeNs - startTimeNs }}, \n" +
            "memory before:\n${info.memoryUsageBefore.string()}, \n" +
            "memory after:\n${info.memoryUsageAfter.string()}, \n" +
            "marked count:${info.markedCount}"
    )
}

@OptIn(NativeRuntimeApi::class)
fun collectGC() = GC.collect()
@OptIn(NativeRuntimeApi::class)
fun scheduleGC() = GC.schedule()