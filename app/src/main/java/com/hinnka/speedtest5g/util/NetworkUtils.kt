package com.hinnka.speedtest5g.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import com.hinnka.speedtest5g.App
import com.hinnka.speedtest5g.R
import com.hinnka.speedtest5g.ext.i18n

object NetworkUtils {

    fun getSimOperator(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.simOperatorName
    }

    fun getNetworkState(context: Context): NetworkType {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return NetworkType.None
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cap = cm.getNetworkCapabilities(cm.activeNetwork)
            val isWifi = cap?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
            if (isWifi) {
                return NetworkType.Wifi.withText(getWifiSSID())
            }
        }
        val activeNetworkInfo = cm.activeNetworkInfo ?: return NetworkType.None
        return when (activeNetworkInfo.type) {
            ConnectivityManager.TYPE_WIFI -> NetworkType.Wifi.withText(getWifiSSID())
            ConnectivityManager.TYPE_MOBILE -> {
                when (activeNetworkInfo.subtype) {
                    TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyManager.NETWORK_TYPE_CDMA,
                    TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_GPRS,
                    TelephonyManager.NETWORK_TYPE_IDEN,
                    TelephonyManager.NETWORK_TYPE_GSM,
                    -> NetworkType._2G.withText(getSimOperator(context))
                    TelephonyManager.NETWORK_TYPE_EVDO_A,
                    TelephonyManager.NETWORK_TYPE_UMTS,
                    TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_EVDO_B,
                    TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_HSPAP,
                    TelephonyManager.NETWORK_TYPE_TD_SCDMA,
                    -> NetworkType._3G.withText(getSimOperator(context))
                    TelephonyManager.NETWORK_TYPE_LTE,
                    -> NetworkType._4G.withText(getSimOperator(context))
                    TelephonyManager.NETWORK_TYPE_NR,
                    -> NetworkType._5G.withText(getSimOperator(context))
                    else -> NetworkType.Mobile.withText(getSimOperator(context))
                }
            }
            else -> NetworkType.Unknown
        }
    }

    @SuppressLint("WifiManagerLeak")
    fun getWifiSSID(): String {
        val wm = App.instance.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return (wm.connectionInfo?.ssid ?: R.string.wifi.i18n).removeSurrounding("\"")
    }
}

enum class NetworkType(var text: String) {
    None(R.string.none.i18n), Wifi(R.string.wifi.i18n), Mobile(R.string.mobile.i18n), _5G("5G"),
    _4G("4G"), _3G("3G"), _2G("2G"), Unknown(R.string.unknown.i18n);

    fun withText(newText: String): NetworkType {
        text = newText
        return this
    }
}