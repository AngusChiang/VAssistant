package cn.vove7.jarvis.plugins

import android.content.Context
import android.net.*
import cn.vove7.android.common.logi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.tools.metro.CommonParams
import cn.vove7.jarvis.tools.metro.ConnectSDK


/**
 * # MetroWlanListener
 *
 * @author Vove
 * @date 2021/7/27
 */
object MetroWlanListener : ConnectivityManager.NetworkCallback() {

    fun start() {
        val conMan = GlobalApp.APP.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        conMan.registerNetworkCallback(
                NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build(), this)
    }

    fun stop() {
        val conMan = GlobalApp.APP.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        conMan.unregisterNetworkCallback(this)
    }

    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        network.logi()
        linkProperties.logi()
        login()
    }

    private fun login() {
        ConnectSDK.getInstance().onInitialSDK(
                GlobalApp.APP,
                CommonParams.Builder("1001001011", "5f08d90700378d7bb0b7f21ce9982af1")
                        .channel("release").build()
        )

        ConnectSDK.getInstance().openNet("", "13100034003") {
        }
    }
}