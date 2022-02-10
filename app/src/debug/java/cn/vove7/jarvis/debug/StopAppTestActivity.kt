package cn.vove7.jarvis.debug

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * # StopAppTestActivity
 *
 * @author Libra
 * @date 2022/2/10
 */
class StopAppTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(Button(this).also {
            it.text = "Launch"
            it.setOnClickListener {
                try {
                    val data = Uri.Builder().scheme("web1n.stopapp")
                        .authority("action")
                        .appendPath("run_app").appendPath("com.huawei.fans")
                        .appendQueryParameter("user", 0.toString()).build()

                   startActivity(Intent("android.intent.action.VIEW").setData(data))

                } catch (e: Throwable) {
                    e.printStackTrace()
                }

            }
        })
    }
}