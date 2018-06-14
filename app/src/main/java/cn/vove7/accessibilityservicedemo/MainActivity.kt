package cn.vove7.accessibilityservicedemo

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import cn.vove7.vtp.runtimepermission.PermissionUtils.accessibilityServiceEnabled
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        App.instance.mainActivity = this
        super.onCreate(savedInstanceState)
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

    fun openAccessibility(view: View) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }


    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        if (accessibilityServiceEnabled(this)) {
            accessibility_status.text = "已开启"
        } else {
            accessibility_status.text = "未开启"
        }
    }


    fun findByID(view: View) {
        if (MyAccessibilityService.isOk()) {
            val nodeInfos = MyAccessibilityService.accessibilityService?.findNodeById(view_id.text.toString())
            if (nodeInfos?.isNotEmpty() == true) {
                Toast.makeText(this, "Found It findByID", Toast.LENGTH_SHORT).show()
                for (nodeInfo in nodeInfos) {
                    Log.d("Vove :", "findByID  ----> " + nodeInfo.node.describeContents())
                }
            } else {
                Toast.makeText(this, "404 Not Found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "无障碍未开启", Toast.LENGTH_SHORT).show()
        }
    }

    fun clickView(view: View) {
        if (MyAccessibilityService.isOk()) {
            val nodeInfos = MyAccessibilityService.accessibilityService?.findNodeById(view_id.text.toString())
            if (nodeInfos?.isNotEmpty() == true) {
                Toast.makeText(this, "Click It", Toast.LENGTH_SHORT).show()
                nodeInfos[0].click()
            } else {
                Toast.makeText(this, "404 Not Found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "无障碍未开启", Toast.LENGTH_SHORT).show()
        }
    }

    fun go2Test(view: View) {
        startActivity(Intent(this, ScriptTestActivity::class.java))
    }
    fun go2Voice(view: View) {
        startActivity(Intent(this, VoiceTestActivity::class.java))
    }
}
