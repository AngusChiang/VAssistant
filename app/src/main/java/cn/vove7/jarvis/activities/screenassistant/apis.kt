package cn.vove7.jarvis.activities.screenassistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * # all
 *
 * @author 11324
 * 2019/3/21
 */
//屏幕内容识别
class SpotScreenActivity : BaseFunActivity() {
    override val id: Int = 0
}
//文字识别
class ScreenOcrActivity : BaseFunActivity() {
    override val id: Int = 1
}

class ScreenShareActivity : BaseFunActivity() {
    override val id: Int = 3
}

class QrCodeActivity : BaseFunActivity() {
    override val id: Int = 4
}


abstract class BaseFunActivity : Activity() {
    abstract val id: Int
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, ScreenAssistActivity::class.java).also {
            it.putExtra("fun_id", id)
            it.putExtras(intent)
        })
        finish()
    }
}
