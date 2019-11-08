package cn.vove7.jarvis.activities.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.SCREEN_ASSIST_QR
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.SCREEN_ASSIST_SCREEN_OCR
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.SCREEN_ASSIST_SCREEN_SHARE
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.SCREEN_ASSIST_SPOT_SCREEN
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.SCREEN_ASSIST_TEXT_PICKER
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.SET_ASSIST_APP
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.SWITCH_VOICE_WAKEUP
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.WAKEUP_SCREEN_ASSIST
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.list.listItems

/**
 * # CreateShortcutActivity
 *
 * @author Vove
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
                        getString(R.string.label_screen_assistant),
                        "文字提取",
                        "二维码识别",
                        "屏幕识别",
                        "屏幕分享",
                        "文字识别"
//                        "语音搜索"
                ), waitForPositiveButton = false) { d, i, t ->
                    val text = t.toString()
                    when (i) {
                        0 -> create(VoiceAssistActivity.WAKE_UP, text)
                        1 -> create(SWITCH_VOICE_WAKEUP, text)
                        2 -> create(SET_ASSIST_APP, text)
                        3 -> create(WAKEUP_SCREEN_ASSIST, text)
                        4 -> create(SCREEN_ASSIST_TEXT_PICKER, text)
                        5 -> create(SCREEN_ASSIST_QR, text)
                        6 -> create(SCREEN_ASSIST_SPOT_SCREEN, text)
                        7 -> create(SCREEN_ASSIST_SCREEN_SHARE, text)
                        8 -> create(SCREEN_ASSIST_SCREEN_OCR, text)
//                        3 -> createWebSearch()
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


    private fun createWebSearch() {
        val intent = Intent(RecognizerIntent.ACTION_WEB_SEARCH)
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "语音搜索")
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent)
        val iconResource = Intent.ShortcutIconResource
                .fromContext(this, R.mipmap.ic_launcher_vassist)
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
        setResult(RESULT_OK, intent)
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