package cn.vove7.jadb.debug

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * # Check5555Activity
 *
 * @author Vove
 * @date 2021/8/13
 */
class Check5555Activity : AppCompatActivity() {

    private val tv by lazy { TextView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(tv)

        thread {
            val rt = Runtime.getRuntime()
            while (true) {
                val p = rt.exec("getprop service.adb.tcp.port")
                var t = String(p.inputStream.readBytes())
                if(t.isBlank()) {
                    t = "empty"
                }
                runOnUiThread {
                    tv.text = t
                }
                if (t.trimEnd() == "5555") {
                    runOnUiThread {
                        Toast.makeText(this, "tcpip 5555 ok", Toast.LENGTH_SHORT).show()
                    }
                    break
                }
                sleep(500)
            }
        }

    }
}