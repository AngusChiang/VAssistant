package cn.vove7.jarvis.receivers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import cn.vove7.common.app.GlobalApp


/**
 * # BTConnectListener
 *
 * @author Administrator
 * 2018/10/26
 */
class BTConnectListener : BroadcastReceiver() {

    private val intentFilter: IntentFilter by lazy {
        val i = IntentFilter()
        i.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        i
    }

    val context: Context get() = GlobalApp.APP

    private fun initBlueToothHeadset() {
        val adapter: BluetoothAdapter
        val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = bm.adapter
        adapter.getProfileProxy(context, blueHeadsetListener, BluetoothProfile.HEADSET)
    }

    var bluetoothHeadset: BluetoothHeadset? = null
    var blueHeadsetListener: BluetoothProfile.ServiceListener = object : BluetoothProfile.ServiceListener {

        override fun onServiceDisconnected(profile: Int) {
            Log.i("blueHeadsetListener", "onServiceDisconnected:$profile")
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = null
            }
        }

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            Log.i("blueHeadsetListener", "onServiceConnected:$profile")
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = proxy as BluetoothHeadset
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {

            }
        }
    }
}