package com.hinnka.speedtest5g.ui.home

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.hinnka.speedtest5g.ext.logD
import com.hinnka.speedtest5g.ext.logE
import com.hinnka.speedtest5g.ui.theme.SpeedTest5GTheme
import com.hinnka.speedtest5g.util.NetworkType
import com.hinnka.speedtest5g.util.NetworkUtils
import com.hinnka.speedtest5g.widget.SpeedAppbar
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MainActivity : AppCompatActivity() {

    private val REQUEST_CHECK_SETTINGS = 0x111
    val viewModel: MainViewModel by viewModels()
    val networkState = mutableStateOf(NetworkType.Unknown)

    lateinit var locClient: FusedLocationProviderClient
    var locCallback: LocationCallback? = null

    val defaultLocationRequest = LocationRequest.create().apply {
        interval = 5000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    var locationSettingsContinuation: CancellableContinuation<Location?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            val state = rememberScaffoldState()
            SpeedTest5GTheme {
                Scaffold(
                    scaffoldState = state,
                    topBar = {
                        SpeedAppbar(state = state)
                    },
                    drawerContent = { Text(text = "Drawer") }
                ) {
                    MainContent(viewModel, networkState)
                }
            }
        }

        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            networkState.value = NetworkUtils.getNetworkState(this@MainActivity)
            lifecycleScope.launchWhenCreated {
                viewModel.locationData.value = requestLocation()
                viewModel.fetchData()
            }
        }.launch(
            arrayOf(
                ACCESS_COARSE_LOCATION,
                ACCESS_FINE_LOCATION,
            )
        )
    }

    suspend fun requestLocation(): Location? {
        logD("requestLocation")
        return suspendCancellableCoroutine { continuation ->
            if (ActivityCompat.checkSelfPermission(
                    this,
                    ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                logD("location permission denied")
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            locClient.lastLocation.addOnSuccessListener {
                if (it == null) {
                    lifecycleScope.launchWhenCreated {
                        continuation.resume(requestLocationSettings())
                    }
                    return@addOnSuccessListener
                }
                logD("location: $it")
                continuation.resume(it)
            }
        }
    }

    @RequiresPermission(anyOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    suspend fun requestLocationSettings(): Location? {
        logD("requestLocationSettings")
        return suspendCancellableCoroutine { continuation ->
            val builder = LocationSettingsRequest.Builder().addLocationRequest(defaultLocationRequest)
            val client: SettingsClient = LocationServices.getSettingsClient(this)
            client.checkLocationSettings(builder.build()).addOnSuccessListener {
                logD("location settings success")
                lifecycleScope.launchWhenCreated {
                    continuation.resume(requestLocationUpdates())
                }
            }.addOnFailureListener { exception ->
                logE("location settings failed", throwable = exception)
                if (exception is ResolvableApiException){
                    try {
                        exception.startResolutionForResult(this@MainActivity,
                            REQUEST_CHECK_SETTINGS)
                        locationSettingsContinuation = continuation
                    } catch (sendEx: IntentSender.SendIntentException) {
                        continuation.resume(null)
                    }
                } else {
                    continuation.resume(null)
                }
            }
        }
    }

    @RequiresPermission(anyOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    suspend fun requestLocationUpdates(): Location? {
        logD("requestLocationUpdates")
        return suspendCancellableCoroutine { continuation ->
            locCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    logD("location: ${locationResult.lastLocation}")
                    if (!continuation.isCompleted) {
                        continuation.resume(locationResult.lastLocation)
                    }
                    locCallback?.let { callback ->
                        locClient.removeLocationUpdates(callback)
                    }
                }
            }
            locClient.requestLocationUpdates(defaultLocationRequest, locCallback!!, Looper.getMainLooper())
            lifecycleScope.launchWhenCreated {
                delay(10_000)
                if (!continuation.isCompleted) {
                    continuation.resume(null)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK) {
            logD("request settings success")
            lifecycleScope.launchWhenCreated {
                locationSettingsContinuation?.resume(requestLocationUpdates())
                locationSettingsContinuation = null
            }
        }
    }
}
