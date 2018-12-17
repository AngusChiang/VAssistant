package cn.vove7.jarvis.activities.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.SWITCH_VOICE_WAKEUP
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.list.listItemsSingleChoice

/**
 * # CreateShortcutActivity
 *
 * @author Administrator
 * 2018/11/13
 */
class CreateShortcutActivity : Activity() {

    val dialog: MaterialDialog by lazy {
        MaterialDialog(this)
                .title(text = "选择快捷方式")
                .listItemsSingleChoice(items = listOf("快速唤醒", "切换语音唤醒"),
                        waitForPositiveButton = true) { d, i, _ ->
                    when (i) {
                        0 -> createWakeupShortcut()
                        1 -> createSwitchVoiceWakeupShortcut()
                    }
                }
                .positiveButton()
                .negativeButton()
                .onDismiss {
                    finishAndRemoveTask()
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog.show()
    }

    private fun createWakeupShortcut() {
        create(Intent.ACTION_VOICE_COMMAND, "唤醒")
    }

    private fun createSwitchVoiceWakeupShortcut() {
        create(SWITCH_VOICE_WAKEUP, "语音唤醒")
    }

    private fun create(action: String, text: String) {
        val sIntent = Intent(this, VoiceAssistActivity::class.java)
        sIntent.action = action
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, text)
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, sIntent)
        val iconResource = Intent.ShortcutIconResource
                .fromContext(this, R.mipmap.ic_launcher_vassist)
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
        setResult(RESULT_OK, intent)
    }

}