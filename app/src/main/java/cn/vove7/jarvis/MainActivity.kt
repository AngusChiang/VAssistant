package cn.vove7.jarvis

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import cn.vove7.executorengine.model.ScreenMetrics

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ScreenMetrics.initIfNeeded(this)
        setContentView(R.layout.activity_main)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val home = Intent(Intent.ACTION_MAIN)
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            home.addCategory(Intent.CATEGORY_HOME)
            startActivity(home)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    fun go2Test(view: View) {
        startActivity(Intent(this, ScriptTestActivity::class.java))
    }

    fun go2Voice(view: View) {
        startActivity(Intent(this, VoiceTestActivity::class.java))
    }

    fun permissionMan(v: View) {
        startActivity(Intent(this, PermissionManagerActivity::class.java))
    }
}
