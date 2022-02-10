package cn.vove7.jarvis.debug

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import cn.vove7.jadb.debug.AdbActivity
import cn.vove7.jadb.debug.AdbPairActivity
import cn.vove7.jadb.debug.AdbShellActivity
import cn.vove7.jarvis.activities.QRScanActivity2

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
        AdbActivity::class,
        AdbShellActivity::class,
        AdbPairActivity::class,
        ScrcpyTestActivity::class,
        StopAppTestActivity::class
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