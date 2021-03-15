package com.hinnka.speedtest5g.data

import com.hinnka.speedtest5g.model.SpeedTestServer
import com.hinnka.speedtest5g.model.SpeedTestSettings

class SpeedTestServerRepository: Repository() {

    suspend fun getServerList(): List<SpeedTestServer> {
        val settings = get(serverListUrl)?.xml(SpeedTestSettings::class.java) ?: return emptyList()
        return settings.servers.serverList
    }

    companion object {
        const val serverListUrl = "http://www.speedtest.net/speedtest-servers.php"
    }
}