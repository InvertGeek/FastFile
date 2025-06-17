package com.donut.fastfile.util.objects

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.donut.fastfile.serverAddress
import com.donut.fastfile.util.getIpAddressInLocalNetwork
import mobile.Mobile

object NetworkChangeReceiver : BroadcastReceiver() {

    var isWifi by mutableStateOf(false)

    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

        return networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            isWifi = isWifiConnected(context)
        }

        serverAddress = "http://${getIpAddressInLocalNetwork()}:${Mobile.getPort()}"
    }
}