package cn.vove7.jarvis.activities.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import cn.vove7.jarvis.R

/**
 * # CreateShortActivity
 *
 * @author Administrator
 * 2018/11/13
 */
class CreateShortActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sIntent = Intent(this, VoiceAssistActivity::class.java)
        sIntent.action = "wakeup"
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "唤醒")
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, sIntent)
        val iconResource = Intent.ShortcutIconResource
                .fromContext(this, R.mipmap.ic_launcher_vassist)
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
        setResult(RESULT_OK, intent)
        finishAndRemoveTask()
    }
}