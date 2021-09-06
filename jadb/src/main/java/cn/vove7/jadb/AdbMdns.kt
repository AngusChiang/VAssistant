package cn.vove7.jadb

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import androidx.lifecycle.MutableLiveData
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket

class AdbMdns(
    context: Context,
    private val serviceType: String,
    private val port: MutableLiveData<Int>
) : NsdManager.DiscoveryListener {

    private var registered = false
    private var running = false
    private var serviceName: String? = null

    private val nsdManager: NsdManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.getSystemService(NsdManager::class.java)
    } else {
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    fun start() {
        running = true
        if (!registered) {
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, this)
        }
    }

    fun stop() {
        running = false
        if (registered) {
            nsdManager.stopServiceDiscovery(this)
        }
    }

    override fun onDiscoveryStarted(serviceType: String?) {
        registered = true
    }

    override fun onDiscoveryStopped(serviceType: String?) {
        registered = false
    }

    override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
    }

    override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
    }

    override fun onServiceFound(info: NsdServiceInfo) {
        nsdManager.resolveService(info, ResolveListener(this))
    }

    override fun onServiceLost(info: NsdServiceInfo) {
        if (info.serviceName == serviceName) port.postValue(-1)
    }

    private fun onServiceResolved(resolvedService: NsdServiceInfo) {
        if (running && NetworkInterface.getNetworkInterfaces()
                .asSequence()
                .any { networkInterface ->
                    networkInterface.inetAddresses
                        .asSequence()
                        .any { resolvedService.host.hostAddress == it.hostAddress }
                }
            && isPortAvailable(resolvedService.port)
        ) {
            serviceName = resolvedService.serviceName
            port.postValue(resolvedService.port)
        }
    }

    private fun isPortAvailable(port: Int) = try {
        ServerSocket().use {
            it.bind(InetSocketAddress(InetAddress.getLoopbackAddress(), port), 1)
            false
        }
    } catch (e: IOException) {
        true
    }

    internal class ResolveListener(private val adbMdns: AdbMdns) : NsdManager.ResolveListener {
        override fun onResolveFailed(nsdServiceInfo: NsdServiceInfo, i: Int) {}

        override fun onServiceResolved(nsdServiceInfo: NsdServiceInfo) {
            adbMdns.onServiceResolved(nsdServiceInfo)
        }

    }

    companion object {
        const val TLS_CONNECT = "_adb-tls-connect._tcp"
        const val TLS_PAIRING = "_adb-tls-pairing._tcp"
    }

}