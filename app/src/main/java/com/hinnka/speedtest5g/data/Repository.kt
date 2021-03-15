package com.hinnka.speedtest5g.data

import com.hinnka.speedtest5g.ext.logE
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import org.simpleframework.xml.core.Persister
import java.io.IOException
import kotlin.coroutines.resume

abstract class Repository {
    val okHttp = OkHttpClient()

    @Throws(IOException::class)
    suspend fun get(url: String): ResponseBody? {
        val request = Request.Builder().url(url).build()
        return suspendCancellableCoroutine { continuation ->
            okHttp.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.cancel(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response.body)
                }
            })
        }
    }

    fun download(url: String, speedBlock: (Float, Boolean) -> Unit) {
        val request = Request.Builder().url(url).build()
        okHttp.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logE("download $url error", throwable = e)
                speedBlock(-1f, true)
            }

            override fun onResponse(call: Call, response: Response) {
                val stream = response.body?.byteStream() ?: return
                val bytes = ByteArray(1024)
                val startTime = System.currentTimeMillis()
                while (stream.read(bytes) != -1) {
                    val speed = 1024 / (System.currentTimeMillis() - startTime) * 1000
                }
            }
        })
    }

}

@Throws(Exception::class)
fun <T> ResponseBody.xml(tClz: Class<out T>): T {
    val persist = Persister()
    return persist.read(tClz, charStream())
}