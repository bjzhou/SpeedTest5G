package com.hinnka.speedtest5g.ext

import android.util.Log

fun logD(vararg message: String) {
    Log.d("SpeedTest5G", message.contentDeepToString())
}

fun logE(vararg message: String, throwable: Throwable? = null) {
    Log.e("SpeedTest5G", message.contentDeepToString(), throwable)
}