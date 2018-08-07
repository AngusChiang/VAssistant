package cn.vove7.jarvis

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import cn.vove7.common.model.ScreenMetrics
import cn.vove7.executorengine.helper.AppHelper
import cn.vove7.executorengine.helper.ContactHelper
import cn.vove7.vtp.runtimepermission.PermissionUtils
import kotlin.concurrent.thread

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        ScreenMetrics.initIfNeeded(this)
        setContentView(R.layout.activity_main)
        thread {
            try {
                if (PermissionUtils.isAllGranted(this@MainActivity, arrayOf("android.permission.READ_CONTACTS")))
                    ContactHelper(this).updateContactList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            AppHelper(this).updateAppList()
        }
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

    fun go2Lua(view: View) {
        startActivity(Intent(this, LuaEditorActivity::class.java))
    }

    fun go2Voice(view: View) {
        startActivity(Intent(this, VoiceTestActivity::class.java))
    }

    fun permissionMan(v: View) {
        startActivity(Intent(this, PermissionManagerActivity::class.java))
    }
}
