package cn.vove7.jadb.debug

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import cn.vove7.jadb.AdbCrypto
import cn.vove7.jadb.R
import java.net.Inet4Address
import kotlin.concurrent.thread

/**
 * # AdbPairActivity
 *
 * @author Vove
 * @date 2021/8/23
 */
@RequiresApi(Build.VERSION_CODES.R)
class AdbPairActivity : AppCompatActivity() {

    private val port = MutableLiveData(0)


    private val con_port = MutableLiveData<Int>()


    private val adbMdns by lazy {
        cn.vove7.jadb.AdbMdns(this, cn.vove7.jadb.AdbMdns.TLS_PAIRING, port)
    }

    //35817
    private val portView by lazy {
        findViewById<TextView>(R.id.port_view)
    }

    private val pairButton by lazy {
        findViewById<Button>(R.id.pair_btn)
    }
    private val pairCodeView by lazy {
        findViewById<EditText>(R.id.pair_code_view)
    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adb_pair)
        port.observe(this, {
            portView.text = it.toString()
        })
        pairButton.setOnClickListener {
            doPair(port.value!!, pairCodeView.text.toString().trim())
        }
        findViewById<View>(R.id.con_btn).setOnClickListener {
            val adbMdns = cn.vove7.jadb.AdbMdns(this, cn.vove7.jadb.AdbMdns.TLS_CONNECT, con_port)
            adbMdns.start()
            con_port.observe(this) {
                if(it !in 1..65534) {
                    return@observe
                }
                Log.d("ADB", "con port: $it")
                adbMdns.stop()
                thread {
                    val adb = cn.vove7.jadb.AdbClient(
                        this,
                        Inet4Address.getLoopbackAddress().hostName,
                        it, AdbCrypto.get(this)
                    )
                    try {
                        adb.connect()
                        val s = adb.shellCommand("top")
                        while (!s.closed);

                        toast("connect success")
//                        adb.tcpip(5555)
                    } catch (E: Throwable) {
                        E.printStackTrace()
                        toast("connect failed")
                    }
                }
            }
        }
        adbMdns.start()
    }

    private fun toast(m: String) = runOnUiThread {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
    }

    private fun doPair(port: Int, code: String) = thread {
        val c = cn.vove7.jadb.AdbPairingClient(
            this,
            Inet4Address.getLoopbackAddress().hostName,
            port, code
        )
        val s = c.start()

        Log.d("ADBPAIR", "succ: $s")
    }

    override fun onDestroy() {
        adbMdns.stop()
        super.onDestroy()
    }
}