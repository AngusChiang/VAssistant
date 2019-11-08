package cn.vove7.jarvis.tools

import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.speech.RecognizerIntent
import androidx.annotation.RequiresApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalApp.Companion.getString
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.utils.newTask
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.VoiceAssistActivity
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.SET_ASSIST_APP
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.SWITCH_DEBUG_MODE
import cn.vove7.jarvis.activities.base.VoiceAssistActivity.Companion.SWITCH_VOICE_WAKEUP


/**
 * # ShortcutUtil
 *
 * @author Administrator
 * 2018/10/8
 */
object ShortcutUtil {
    val context: Context
        get() = GlobalApp.APP

    private val shortManager: ShortcutManager?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
        } else {
            null
        }

    /**
     * 添加快捷唤醒
     */
    fun addWakeUpPinShortcut() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> addPinedShortcut(wakeUpShortcut!!)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 -> addShortcut(wakeUpShortcut!!)
            else -> GlobalApp.toastWarning("需要7.1+")
        }
    }

    fun addDebugShortcut() {
        addShortcut(debugShortcut!!)
    }

    private val wakeUpShortcut: ShortcutInfo?
        get() = buildShortcut(
                "fast_wakeup",
                getString(R.string.shortcut_wakeup),
                Intent(Intent.ACTION_ASSIST, null, context, VoiceAssistActivity::class.java),
                3
        )

    private val debugShortcut: ShortcutInfo?
        get() = buildShortcut(
                "debug_mode",
                "调试模式",
                Intent(SWITCH_DEBUG_MODE, null, context, VoiceAssistActivity::class.java)
                , 4
        )

    private val switchVoiceWpShortcut: ShortcutInfo?
        get() = buildShortcut(
                "switch_voice_wp",
                getString(R.string.text_switch_voice_wp),
                Intent(SWITCH_VOICE_WAKEUP, null, context, VoiceAssistActivity::class.java),
                2
        )

    private val oneKeySetAssistShortcut: ShortcutInfo?
        get() = buildShortcut(
                "one_key_set_assist",
                getString(R.string.shortcut_label_set_assist_app),
                Intent(SET_ASSIST_APP, null, context, VoiceAssistActivity::class.java),
                1
        )
//        get() =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
//                ShortcutInfo.Builder(context, "one_key_set_assist")    //添加shortcut id
//                        .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher_vassist)) //添加标签图标
//                        .setShortLabel(getString(R.string.shortcut_label_set_assist_app)) // 短标签名
//                        .setLongLabel(getString(R.string.shortcut_label_set_assist_app))  //长标签名
//                        .setIntent(Intent(SET_ASSIST_APP, null, context, VoiceAssistActivity::class.java))  //action
//                        .setDisabledMessage(getString(R.string.text_not_enable)) //disable后提示
//                        .setRank(1) //位置
//                        .build()
//            } else null

    private val webSearchShortcut
        get() = buildShortcut(
                "sc_web_search",
                "语音搜索",
                Intent(RecognizerIntent.ACTION_WEB_SEARCH).newTask(),
                5
        )

    private fun buildShortcut(id: String, label: String, intent: Intent, rank: Int): ShortcutInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutInfo.Builder(context, id)    //添加shortcut id
                    .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher_vassist)) //添加标签图标
                    .setShortLabel(label) // 短标签名
                    .setLongLabel(label)  //长标签名
                    .setIntent(intent)  //action
                    .setDisabledMessage(getString(R.string.text_not_enable)) //disable后提示
                    .setRank(rank) //位置
                    .build()
        } else null

    }

    /**
     * 添加快捷唤醒
     */
    fun initShortcut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            addShortcut(wakeUpShortcut!!)
            addShortcut(switchVoiceWpShortcut!!)
            addShortcut(oneKeySetAssistShortcut!!)
            if (BuildConfig.DEBUG) {
                addDebugShortcut()
            }
//            addShortcut(webSearchShortcut!!)
        }
//        } else {
//            GlobalApp.toastInfo("需要8.0+")
//        }
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private fun addShortcut(shortCutInfo: ShortcutInfo) {
        if (existsCut(shortCutInfo.id)) return

        addShortcut(listOf(shortCutInfo))
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun existsCut(id: String): Boolean {
        shortManager?.dynamicShortcuts?.forEach {
            if (it.id == id) return true
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun addShortcut(shortCutInfo: List<ShortcutInfo>): Boolean {
        return try {
            checkLimit()
            shortManager?.addDynamicShortcuts(shortCutInfo) //创建2个快捷方式
            true
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalApp.toastWarning("添加失败，可能数量超出限制")
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun checkLimit() {
        val nowCount = shortManager?.dynamicShortcuts?.size
        if (nowCount == shortManager?.maxShortcutCountPerActivity) {//超出，移除
            shortManager?.removeDynamicShortcuts(listOf(shortManager!!.dynamicShortcuts[1].id))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addPinedShortcut(info: ShortcutInfo) {
        val pinnedShortcutCallbackIntent = shortManager?.createShortcutResultIntent(info)

        val successCallback = PendingIntent.getBroadcast(context, 0,
                pinnedShortcutCallbackIntent, 0)

        if (shortManager?.requestPinShortcut(info, successCallback.intentSender) == true) {
//            GlobalApp.toastInfo("添加完成")
        } else
            GlobalApp.toastError("添加失败")
    }

    /**
     * 同时添加进图标icon
     *
     * @param actionNode ActionNode
     * @param add2Icon Boolean
     */
    fun addActionShortcut(actionNode: ActionNode, add2Icon: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val info = ShortcutInfo.Builder(context, "${actionNode.id}")
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_play_arrow))
                    .setShortLabel(actionNode.actionTitle ?: "无标题") // 短标签名
                    .setLongLabel(actionNode.actionTitle ?: "无标题")  //长标签名
                    .setIntent(Intent("${actionNode.id}", null, context, VoiceAssistActivity::class.java))  //action
                    .setDisabledMessage(getString(R.string.text_not_enable)) //disable后提示
                    .setRank(0) //位置
                    .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                addPinedShortcut(info)
                if (add2Icon)
                    addShortcut(info)
            } else {
                addShortcut(info)
            }
        } else {
            GlobalApp.toastWarning("需要7.1+")
        }
    }
}