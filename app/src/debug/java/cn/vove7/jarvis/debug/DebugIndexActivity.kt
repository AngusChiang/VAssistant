package cn.vove7.jarvis.debug

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import cn.vove7.jarvis.activities.QRScanActivity2
import cn.vove7.jarvis.jadb.debug.AdbActivity

/**
 * # DebugIndexActivity
 *
 * @author Vove
 * @date 2021/8/14
 */
class DebugIndexActivity : AppCompatActivity() {

    private val acts = listOf(
            QRScanActivity2::class,
            MediaControllerActivity::class,
            AdbActivity::class
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lv = ListView(this)

        setContentView(lv)

        lv.adapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1, acts)

        lv.setOnItemClickListener { _, _, position, _ ->
            startActivity(Intent(this, acts[position].java))
        }

    }
}