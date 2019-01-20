package cn.vove7.jarvis.activities.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.newTask
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.SET_ASSIST_APP
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.SWITCH_VOICE_WAKEUP
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.list.listItems

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
                .listItems(items = listOf(
                        getString(R.string.shortcut_wakeup),
                        getString(R.string.text_switch_voice_wp),
                        getString(R.string.shortcut_label_set_assist_app),
                        "语音搜索"),
                        waitForPositiveButton = false) { d, i, _ ->
                    when (i) {
                        0 -> createWakeupShortcut()
                        1 -> createSwitchVoiceWakeupShortcut()
                        2 -> createOneKeySetAssistApp()
                        3 -> createWebSearch()
                    }
                    d.dismiss()
                }
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
    private fun createWebSearch() {
        val intent = Intent(RecognizerIntent.ACTION_WEB_SEARCH)
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "语音搜索")
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent)
        val iconResource = Intent.ShortcutIconResource
                .fromContext(this, R.mipmap.ic_launcher_vassist)
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
        setResult(RESULT_OK, intent)
    }

    private fun createSwitchVoiceWakeupShortcut() {
        create(SWITCH_VOICE_WAKEUP, "语音唤醒")
    }

    private fun createOneKeySetAssistApp() {
        create(SET_ASSIST_APP, "一键设为助手应用")
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