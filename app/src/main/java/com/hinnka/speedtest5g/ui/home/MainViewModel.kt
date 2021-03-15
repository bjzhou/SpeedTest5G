package com.hinnka.speedtest5g.ui.home

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hinnka.speedtest5g.data.SpeedTestServerRepository
import com.hinnka.speedtest5g.ext.ioScope
import com.hinnka.speedtest5g.ext.logD
import com.hinnka.speedtest5g.ext.logE
import com.hinnka.speedtest5g.model.SpeedTestServer
import com.hinnka.speedtest5g.model.pingAndSort
import com.hinnka.speedtest5g.model.sort
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.IRepeatListener
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.ComputationMethod
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {

    val repository = SpeedTestServerRepository()
    val serverListData = MutableLiveData<List<SpeedTestServer>>(emptyList())
    val locationData = MutableLiveData<Location>()
    val testSocket = SpeedTestSocket().apply {
        downloadSetupTime = TimeUnit.MILLISECONDS.toNanos(100)
        uploadSetupTime = TimeUnit.MILLISECONDS.toNanos(100)
    }

    suspend fun fetchData() {
        try {
            val list = repository.getServerList()
            serverListData.value = locationData.value?.let {
                list.sort(it.longitude, it.latitude)
            } ?: list.pingAndSort()
        } catch (e: Exception) {
            logE("fetch server list error", throwable = e)
        }
    }

    fun download(url: String, block: (SpeedTestReport, Boolean) -> Unit) {
        ioScope.launch {
            logD("start download $url")
            val listener = object : ISpeedTestListener {
                override fun onCompletion(report: SpeedTestReport) {
                }

                override fun onProgress(percent: Float, report: SpeedTestReport) {
                    logD("download onProgress $percent")
                    block(report, false)
                }

                override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {
                    logE("download $url error $speedTestError $errorMessage")
                }
            }
            testSocket.addSpeedTestListener(listener)
            testSocket.startDownloadRepeat(url, 20_000, object : IRepeatListener {
                override fun onCompletion(report: SpeedTestReport) {
                    logD("download finished")
                    block(report, true)
                    testSocket.removeSpeedTestListener(listener)
                }

                override fun onReport(report: SpeedTestReport) {
//                    logD("download repeat report: ${report.transferRateBit.toFloat() / 1_000}")
                    block(report, false)
                }
            })
        }
    }

    fun upload(url: String, block: (SpeedTestReport, Boolean) -> Unit) {
        ioScope.launch {
            val listener = object : ISpeedTestListener {
                override fun onCompletion(report: SpeedTestReport) {
                }

                override fun onProgress(percent: Float, report: SpeedTestReport) {
                    block(report, false)
                }

                override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {
                    logE("upload $url error $speedTestError $errorMessage")
                }
            }
            testSocket.addSpeedTestListener(listener)
            testSocket.startUploadRepeat(url, 30_000, 10_000_000, object : IRepeatListener {
                override fun onCompletion(report: SpeedTestReport) {
                    block(report, true)
                    testSocket.removeSpeedTestListener(listener)
                }

                override fun onReport(report: SpeedTestReport) {
                    block(report, false)
                }
            })
        }
    }
}