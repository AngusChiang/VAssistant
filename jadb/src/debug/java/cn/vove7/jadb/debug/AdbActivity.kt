package cn.vove7.jadb.debug

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import cn.vove7.jadb.AdbClient
import cn.vove7.jadb.AdbCrypto
import cn.vove7.jadb.AdbMdns
import cn.vove7.jadb.R
import java.net.Inet4Address
import kotlin.concurrent.thread

/**
 * # AdbActivity
 *
 * @author Vove
 * @date 2021/8/12
 */
class AdbActivity : AppCompatActivity() {

    private val logView: TextView by lazy {
        findViewById(R.id.log_view)
    }

    lateinit var adbClient: AdbClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adb)

        val xon = findViewById<View>(R.id.connect_btn)
        xon.setOnClickListener {
            val (host, port) = findViewById<EditText>(R.id.host_et).text.toString().split(":")

            thread {
                adbClient = AdbClient(this, host, port.toInt(), AdbCrypto.get(this))
                kotlin.runCatching {
                    adbClient.connect()
                    logv("connect success")
                }.onFailure {
                    logv("connect failed")
                }
            }
        }
        xon.setOnLongClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                autoPairConnect()
            }
            true
        }
        findViewById<View>(R.id.run_btn).setOnClickListener {
            if (::adbClient.isInitialized) {
                val cmd = findViewById<EditText>(R.id.cmd_et).text.toString()
                thread {
                    val s = adbClient.shellCommand(cmd)
                    s.onClose {
                        logv(String(data))
                        logv("closed $localId")
                    }
                }
            } else {
                logv("has not connected")
            }
        }
    }

    private fun logv(s: Any?) {
        runOnUiThread {
            logView.append(s.toString() + "\n")
        }
    }

    val con_port = MutableLiveData(0)

    @RequiresApi(Build.VERSION_CODES.R)
    fun autoPairConnect() {
        val adbMdns = AdbMdns(this, AdbMdns.TLS_CONNECT, con_port)
        adbMdns.start()
        con_port.observe(this) {
            if (it !in 1..65534) {
                return@observe
            }
            Log.d("ADB", "con port: $it")
            adbMdns.stop()
            thread {
                val adb = AdbClient(
                    this,
                    Inet4Address.getLoopbackAddress().hostName,
                    it, AdbCrypto.get(this)
                )
                try {
                    adb.connect()
                    val s = adb.shellCommand("top")
                    s.interrupt()
                    s.close()
                    toast("connect success")
                } catch (E: Throwable) {
                    E.printStackTrace()
                    toast("connect failed")
                }
            }

        }
    }

    override fun onDestroy() {
        if (::adbClient.isInitialized) {
            adbClient.close()
        }
        super.onDestroy()
    }

    private fun toast(m: String) = runOnUiThread {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
    }


}