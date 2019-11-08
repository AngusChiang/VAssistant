package cn.vove7.jarvis.receivers

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.services.MainService
import cn.vove7.vtp.log.Vog


/**
 * # BTConnectListener
 *
 * @author Administrator
 * 2018/10/26
 */
object BTConnectListener : DyBCReceiver() {

    override val intentFilter: IntentFilter
        get() = IntentFilter().apply {
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
        }

    val context: Context get() = GlobalApp.APP
    var bluetoothHeadset: BluetoothHeadset? = null
    var bluetoothDevice: BluetoothDevice? = null

    fun useBTRecorderIf() {
        val bh = bluetoothHeadset
        val bd = bluetoothDevice

        if (bh != null && bd != null) {//录音
            Vog.d("useBTRecorderIf ---> ok")
            bh.startVoiceRecognition(bd)
        }
    }

    fun stopBTRecorderIf() {
        val bh = bluetoothHeadset
        val bd = bluetoothDevice

        if (bh != null && bd != null) {//录音
            Vog.d("useBTRecorderIf ---> ok")
            bh.stopVoiceRecognition(bd)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Vog.d(intent?.action)
        when (intent?.action) {
            BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
                val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1)

                Vog.d("BluetoothHeadset ---> $state")

                if (state == BluetoothProfile.STATE_CONNECTED) {
                    if (device == null) return

                    bluetoothDevice = device
                    //config
//                    useBTRecorderIf()//连接
//                    updateBluetoothParameters(true)
                    if (MainService.speechRecogService?.wakeupI?.opened == true) {
//                        MainService.onCommand(AppBus.)
                    }

                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    bluetoothDevice = null
//                    updateBluetoothParameters(false)
                }
            }
            BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1)
                val prevState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1)
                if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    // SCO channel has just become available.
                    // still waiting for the TTS to be set up.
                    // we now have SCO connection and TTS, so we can start.
                } else if (prevState == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    // apparently our connection to the headset has dropped.
                    // we won't be able to continue voice dialing.
                    bluetoothDevice = null
                    bluetoothHeadset = null
                    Vog.d("断开连接")
                }
            }
        }
    }
}