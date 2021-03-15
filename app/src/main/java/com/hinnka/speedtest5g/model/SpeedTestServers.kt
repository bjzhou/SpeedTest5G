package com.hinnka.speedtest5g.model

import com.hinnka.speedtest5g.ext.ioScope
import com.hinnka.speedtest5g.ext.logD
import com.hinnka.speedtest5g.util.LocationUtils
import com.hinnka.speedtest5g.util.PingUtil
import kotlinx.coroutines.*
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import kotlin.coroutines.resume

@Root(name = "settings")
data class SpeedTestSettings @JvmOverloads constructor(
    @field:Element var servers: SpeedTestServers = SpeedTestServers()
)

@Root(name = "servers")
data class SpeedTestServers @JvmOverloads constructor(
    @field:ElementList(entry = "server", inline = true) var serverList: List<SpeedTestServer> = mutableListOf()
)

@Root(name = "server")
data class SpeedTestServer @JvmOverloads constructor(
    @field:Attribute var url: String = "",
    @field:Attribute var lat: String = "",
    @field:Attribute var lon: String = "",
    @field:Attribute var name: String = "",
    @field:Attribute var country: String = "",
    @field:Attribute var cc: String = "",
    @field:Attribute var sponsor: String = "",
    @field:Attribute var id: String = "",
    @field:Attribute var host: String = "",

    var ping: Int = 0,
    var uploadUrl: String = url,
    var downloadUrl: String = url.replace("upload.php", "random2000x2000.jpg")
)

fun List<SpeedTestServer>.sort(lon: Double, lat: Double): List<SpeedTestServer> {
    return sortedWith { o1, o2 ->
        val d1 = LocationUtils.getDistance(o1.lon.toDouble(), o1.lat.toDouble(), lon, lat)
        val d2 = LocationUtils.getDistance(o2.lon.toDouble(), o2.lat.toDouble(), lon, lat)
        d1.compareTo(d2)
    }
}

suspend fun List<SpeedTestServer>.pingAndSort(): List<SpeedTestServer> {
    return withContext(ioScope.coroutineContext) {
        forEach { server ->
            val ping = try {
                suspendCancellableCoroutine {
                    launch {
                        val ping = PingUtil.getAvgRTT(server.url, 1, 500)
                        it.resume(ping)
                    }
                }
            } catch (e: Exception) {
                999
            }
            logD("ping: $ping")
            server.ping = ping
        }
        val sortList = sortedWith {o1, o2 ->
            o1.ping.compareTo(o2.ping)
        }
        sortList
    }
}