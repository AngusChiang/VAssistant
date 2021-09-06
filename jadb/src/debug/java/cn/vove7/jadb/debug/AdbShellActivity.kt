package cn.vove7.jadb.debug

import android.content.DialogInterface.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import cn.vove7.jadb.AdbClient
import cn.vove7.jadb.AdbCrypto
import cn.vove7.jadb.databinding.ActivityShellBinding
import java.lang.Thread.sleep
import java.net.Inet4Address
import kotlin.concurrent.thread

/**
 * # AdbShellActivity
 *
 * @author Vove
 * @date 2021/8/25
 */
class AdbShellActivity : AppCompatActivity() {

    val vb by lazy {
        ActivityShellBinding.inflate(layoutInflater)
    }

    lateinit var shellStream: cn.vove7.jadb.AdbStream

    lateinit var adbClient: cn.vove7.jadb.AdbClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        reqConnect()
    }

    private fun reqConnect() {
        val hostIpView = EditText(this)
        hostIpView.setText("127.0.0.1:5555")
        AlertDialog.Builder(this)
            .setTitle("adb server")
            .setCancelable(false)

            .setView(hostIpView)
            .setPositiveButton("连接", null)
            .setNeutralButton("Wireless", null)
            .setNegativeButton("取消") { d, _ ->
                finish()
            }
            .create().apply {
                setOnShowListener { d ->
                    get(BUTTON_POSITIVE).setOnClickListener {
                        val (host, port) = hostIpView.text.toString().trim().split(":")
                        doConnect(host, port.toInt(), d as AlertDialog)
                    }
                    get(BUTTON_NEUTRAL).setOnClickListener {
                        doWirelessConnect(d as AlertDialog)
                    }
                }
                show()
            }
    }

    val con_port = MutableLiveData(0)

    private fun doWirelessConnect(dialog: AlertDialog) {
        val adbMdns = cn.vove7.jadb.AdbMdns(this, cn.vove7.jadb.AdbMdns.TLS_CONNECT, con_port)
        adbMdns.start()
        dialog.setOnDismissListener {
            adbMdns.stop()
        }
        con_port.observe(this) {
            if (it !in 1..65534) {
                return@observe
            }
            Log.d("ADB", "con port: $it")
            adbMdns.stop()
            doConnect(Inet4Address.getLoopbackAddress().hostName, it, dialog)
        }
    }

    private fun doConnect(host: String, port: Int, dialog: AlertDialog) {
        dialog.setTitle("连接中 $host:$port")
        dialog[BUTTON_POSITIVE].isEnabled = false
        dialog[BUTTON_NEUTRAL].isEnabled = false
        dialog[BUTTON_NEGATIVE].isEnabled = false
        thread {
            sleep(500)
            adbClient = AdbClient(this, host, port, AdbCrypto.get(this))
            kotlin.runCatching {
                adbClient.connect()
                shellStream = adbClient.open("shell:")
                shellStream.onData {
                    logText(String(it))
                }
                shellStream.onClose {
                    logText("[$localId]closed")
                    runOnUiThread {
                        reqConnect()
                        vb.shellTv.text = ""
                    }
                }
            }.onSuccess {
                runOnUiThread {
                    Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }.onFailure {
                it.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "连接失败 ${it.message}", Toast.LENGTH_SHORT).show()
                    dialog[BUTTON_POSITIVE].isEnabled = true
                    dialog[BUTTON_NEUTRAL].isEnabled = true
                    dialog[BUTTON_NEGATIVE].isEnabled = true
                }
            }
        }
    }

    private operator fun AlertDialog.get(wb: Int) = getButton(wb)


    private fun initView() {
        setContentView(vb.root)

        vb.sendBtn.setOnClickListener {
            if (!::shellStream.isInitialized) {
                return@setOnClickListener
            }
            if (shellStream.closed) {
                logText("closed")
                return@setOnClickListener
            }
            thread {
                kotlin.runCatching {
                    shellStream.write("${vb.shellCmd.text}\n")
                }
            }
        }

        vb.interruptBtn.setOnClickListener {
            if (!::shellStream.isInitialized) {
                return@setOnClickListener
            }
            thread {
                shellStream.interrupt()
            }
        }

    }

    fun logText(s: String) {
        runOnUiThread {
            vb.shellTv.append(s)
            vb.shellTv.post {
                vb.scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    override fun onDestroy() {
        if (::adbClient.isInitialized) {
            adbClient.close()
        }
        super.onDestroy()
    }
}