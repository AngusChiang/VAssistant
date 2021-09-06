package cn.vove7.common.helper

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import cn.vove7.android.common.logi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import java.lang.Thread.sleep
import java.util.*

/**
 * # ConnectiveNsdHelper
 *
 * @author Vove
 * @date 2021/8/30
 */

data class NsdDevice(val name: String, val host: String, val port: Int)

object ConnectiveNsdHelper : NsdManager.DiscoveryListener {
    private const val NSD_TYPE = "_vassistant_cs._tcp"

    val selfDeviceName = AppConfig.deviceName + "[${Random().nextInt(10000)}]"

    private var started = false

    private val nsdService
        get() = GlobalApp.ForeService.getSystemService(Context.NSD_SERVICE) as NsdManager

    private val devices = hashMapOf<String, NsdDevice>()

    fun start() {
        if (started) return
        nsdService.discoverServices(NSD_TYPE, NsdManager.PROTOCOL_DNS_SD, this)
    }

    fun stop() {
        devices.clear()
        nsdService.stopServiceDiscovery(this)
        started = false
    }

    fun getDevices(): List<NsdDevice> = devices.values.toList()

    override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
        GlobalLog.err("onStartDiscoveryFailed: $errorCode $serviceType")
    }

    override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
        GlobalLog.err("onStopDiscoveryFailed: $errorCode $serviceType")
    }

    override fun onDiscoveryStarted(serviceType: String?) {
        GlobalLog.log("onDiscoveryStarted $serviceType")
        started = true
    }

    override fun onDiscoveryStopped(serviceType: String?) {
        GlobalLog.log("onDiscoveryStopped $serviceType")
        started = false
    }

    override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
        "onServiceFound: $serviceInfo".logi()
        if (serviceInfo?.serviceName == selfDeviceName) {
            return
        }
        nsdService.resolveService(serviceInfo, object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                GlobalLog.err("nsdService.resolveService: $serviceInfo $errorCode")
            }

            override fun onServiceResolved(si: NsdServiceInfo?) {
                "onServiceResolved: $si".logi()
                si ?: return
                devices[si.serviceName] = NsdDevice(si.serviceName, si.host.hostName, si.port)
            }
        })
    }

    override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
        serviceInfo ?: return
        devices.remove(serviceInfo.serviceName)
    }

    fun reset() {
        stop()
        sleep(500)
        start()
    }
}