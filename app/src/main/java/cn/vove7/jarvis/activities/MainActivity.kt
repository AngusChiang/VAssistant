package cn.vove7.jarvis.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.vtp.runtimepermission.PermissionUtils

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AppConfig.checkUser()) {
            finish()
            return
        }

        PermissionUtils.autoRequestPermission(this, arrayOf(
                "android.permission.RECORD_AUDIO",
                "android.permission.INTERNET",
                "android.permission.READ_PHONE_STATE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.FLASHLIGHT",
                "android.permission.CAMERA",
                "android.permission.CALL_PHONE",
                "android.permission.READ_CONTACTS"
        ))

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

    fun go2Lua(view: View) {
        startActivity(Intent(this, LuaEditorActivity::class.java))
    }

    fun go2Voice(view: View) {
        startActivity(Intent(this, VoiceTestActivity::class.java))
    }

    fun go2InstList(v: View) {
        startActivity(Intent(this, InstManagerActivity::class.java))

    }

    fun permissionMan(v: View) {
        startActivity(Intent(this, PermissionManagerActivity::class.java))
    }

    fun go2Setting(v: View) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    fun go2Js(v: View) {
        startActivity(Intent(this, JsEditorActivity::class.java))
    }

    fun go2Welcome(v: View) {
        startActivity(Intent(this, WelcomeActivity::class.java))
    }

    fun onClick(v: View) {

        when (v.id) {
            R.id.button_mark -> {
                startActivity(Intent(this, MarkedManagerActivity::class.java))
            }
            R.id.button_main -> {
                startActivity(Intent(this, RealMainActivity::class.java))
            }

        }

    }
}
