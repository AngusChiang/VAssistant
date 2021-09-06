package cn.vove7.jarvis.tools.debugserver

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.SparseArray
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog

/**
 * # MdnsManager
 *
 * @author Vove
 * @date 2021/8/30
 */
object MdnsManager {

    private val listeners = SparseArray<NsdManager.RegistrationListener>()

    private val nsdService by lazy {
        GlobalApp.APP.getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    fun exportService(port: Int, type: String, name: String = Build.MODEL) {
        nsdService.registerService(NsdServiceInfo().apply {
            this.port = port
            serviceType = type
            serviceName = name
        }, NsdManager.PROTOCOL_DNS_SD, DefaultRegistrationListener(port))
    }

    fun unexportService(port:Int) {
        listeners[port]?.also {
            nsdService.unregisterService(it)
        }
    }

    internal class DefaultRegistrationListener(val port: Int) : NsdManager.RegistrationListener {
        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            GlobalLog.err("onRegistrationFailed: $serviceInfo code: $errorCode")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            GlobalLog.err("onUnregistrationFailed: $serviceInfo code: $errorCode")
        }

        override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
            listeners.put(port, this)
            GlobalLog.log("onServiceRegistered $serviceInfo")
        }

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
            listeners.remove(port)
            GlobalLog.log("onServiceUnregistered $serviceInfo")
        }
    }
}