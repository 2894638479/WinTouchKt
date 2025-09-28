package error

import logger.errorBox
import logger.printError
import platform.posix.exit


inline fun <T> catchInKotlin(block:()->T):T {
    try {
        return block()
    } catch (e:Throwable) {
        var err = e.toString()
        var exception = e.cause
        var count = 0
        while (exception != null && count < 10){
            err += "\ncaused by: $exception"
            exception = exception.cause
            count++
        }
        val extra = "process will exit after this window closed"
        printError(err)
        errorBox(err + "\n\n" + extra)
        exit(1)
        error("this code should not be reached")
    }
}

inline fun <T> catchInKotlin(name: String,block:()->T):T = catchInKotlin({name},block)
inline fun <T> catchInKotlin(name:()-> String,block:()->T):T = catchInKotlin {
    wrapExceptionName(name,block)
}

inline fun <T> wrapExceptionName(name:String,block: () -> T) = wrapExceptionName({name},block)
inline fun <T> wrapExceptionName(name:()->String,block: () -> T):T{
    try {
        return block()
    } catch (e:Throwable) {
        throw Exception(name(),e)
    }
}

fun exitProcess(code:Int): Nothing{
    exit(code)
    error("this code should not be reached. exit code $code")
}