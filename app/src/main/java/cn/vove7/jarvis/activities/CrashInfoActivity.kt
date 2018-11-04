package cn.vove7.jarvis.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import cn.vove7.vtp.sharedpreference.SpHelper
import com.afollestad.materialdialogs.MaterialDialog

/**
 * # CrashInfoActivity
 *
 * @author Administrator
 * 2018/10/30
 */
class CrashInfoActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Toast.makeText(this, "程序出现异常,已重启", Toast.LENGTH_LONG).show()
        restart()
//        MaterialDialog(this).title(text = "异常")
//                .message(text = "程序出现异常")
//                .positiveButton(text = "重启") { restart() }
//                .negativeButton(text = "退出") {
//                    exit()
//                }
//                .show()
    }

    private fun restart() {
        val intent = baseContext.packageManager
                .getLaunchIntentForPackage(baseContext.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}