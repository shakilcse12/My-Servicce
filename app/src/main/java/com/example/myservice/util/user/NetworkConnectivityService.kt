package com.example.myservice.util
import android.content.Context
import android.net.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

sealed class NetworkStatus {
    object Connected : NetworkStatus()
    object Disconnected : NetworkStatus()
}

class ConnectivityService private constructor(context: Context) {
    private val cm = context.applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkStatus.Connected)
            }
            override fun onLost(network: Network) {
                trySend(NetworkStatus.Disconnected)
            }
        }
        cm.registerNetworkCallback(request, cb)
        // Emit initial status
        val current = cm.activeNetwork?.let { net ->
            cm.getNetworkCapabilities(net)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
        trySend(if (current) NetworkStatus.Connected else NetworkStatus.Disconnected)
        awaitClose { cm.unregisterNetworkCallback(cb) }
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    companion object {
        @Volatile private var instance: ConnectivityService? = null
        fun get(context: Context): ConnectivityService =
            instance ?: synchronized(this) {
                instance ?: ConnectivityService(context).also { instance = it }
            }
    }
}
