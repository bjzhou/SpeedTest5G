package com.hinnka.speedtest5g.ui.home

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.hinnka.speedtest5g.R
import com.hinnka.speedtest5g.ext.setTimeout
import com.hinnka.speedtest5g.model.SpeedTestServer
import com.hinnka.speedtest5g.model.TestState
import com.hinnka.speedtest5g.ui.theme.lightWhite
import com.hinnka.speedtest5g.util.NetworkType


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainContent(viewModel: MainViewModel, networkState: MutableState<NetworkType>) {
    val serverListState = viewModel.serverListData.observeAsState(emptyList())
    val (buttonVisible, setButtonVisible) = remember { mutableStateOf(true) }
    val (panVisible, setPanVisible) = remember { mutableStateOf(false) }
    val mbps = remember { mutableStateOf(0f) }
    val testState = remember { mutableStateOf(TestState.Download) }

    val owner = LocalLifecycleOwner.current

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            SpeedTestButton(buttonVisible) {
                setButtonVisible(false)
                setTimeout(200L) {
                    setPanVisible(true)

                    start(viewModel, owner, mbps, testState)
                }
            }
            SpeedTestPan(panVisible, mbps, testState)
        }
        Spacer(modifier = Modifier.size(30.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 20.dp),
            shape = RoundedCornerShape(10.dp),
            backgroundColor = Color(0xff2a4869),
            elevation = 4.dp
        ) {
            Column(Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NetworkIcon(type = networkState.value)
                    Spacer(modifier = Modifier.size(10.dp))
                    Column {
                        Text(text = networkState.value.text)
                        Text(text = Build.MODEL, color = lightWhite)
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ServerIcon()
                    Spacer(modifier = Modifier.size(10.dp))
                    Column {
                        Text(
                            text = serverListState.value.firstOrNull()?.sponsor ?: stringResource(
                                id = R.string.choosing_server
                            )
                        )
                        Text(text = stringResource(id = R.string.change_server), color = Color(0xff40b1e5))
                    }
                }
            }
        }
    }
}

fun start(viewModel: MainViewModel, owner: LifecycleOwner, mbps: MutableState<Float>, testState: MutableState<TestState>) {
    owner.lifecycleScope.launchWhenCreated {
        viewModel.serverListData.observe(owner, object : Observer<List<SpeedTestServer>> {
            override fun onChanged(serverList: List<SpeedTestServer>?) {
                val server = serverList?.firstOrNull()
                server?.let {
                    viewModel.download(server.downloadUrl) { report, finished ->
                        mbps.value = report.transferRateBit.toFloat() / 1_000_000f
                        if (finished) {
                            mbps.value = 0f
                            testState.value = TestState.Upload
                            setTimeout(1000L) {
                                viewModel.upload(server.url) { report2, finished2 ->
                                    mbps.value = report2.transferRateBit.toFloat() / 1_000_000f
                                    if (finished2) {
                                        mbps.value = 0f
                                        testState.value = TestState.Finished
                                    }
                                }
                            }
                        }
                    }
                    viewModel.serverListData.removeObserver(this)
                }
            }
        })
    }
}

@Composable
fun NetworkIcon(type: NetworkType) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .border(1.dp, lightWhite, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            NetworkType.Wifi -> Image(
                painter = painterResource(id = R.drawable.wifi),
                contentDescription = "wifi",
                colorFilter = ColorFilter.tint(lightWhite)
            )
            NetworkType._2G -> Text(text = "2G", color = lightWhite)
            NetworkType._3G -> Text(text = "3G", color = lightWhite)
            NetworkType._4G -> Text(text = "4G", color = lightWhite)
            NetworkType._5G -> Text(text = "5G", color = lightWhite)
            else -> Image(
                painter = painterResource(id = R.drawable.network),
                contentDescription = "network",
                colorFilter = ColorFilter.tint(lightWhite)
            )
        }
    }
}

@Composable
fun ServerIcon() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .border(1.dp, lightWhite, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.server),
            contentDescription = "server",
            colorFilter = ColorFilter.tint(lightWhite)
        )
    }
}