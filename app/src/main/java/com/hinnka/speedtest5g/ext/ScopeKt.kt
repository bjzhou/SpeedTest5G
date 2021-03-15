package com.hinnka.speedtest5g.ext

import kotlinx.coroutines.*

val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

fun setTimeout(delayInMills: Long, block: () -> Unit) {
    GlobalScope.launch {
        delay(delayInMills)
        block()
    }
}