package cn.vove7.jarvis.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.RootHelper
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.ThreadPool
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.adapters.SettingsExpandableAdapter
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.AppLogic
import cn.vove7.jarvis.tools.ShortcutUtil
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.tools.UriUtils
import cn.vove7.jarvis.view.*
import cn.vove7.jarvis.view.custom.SettingGroupItem
import cn.vove7.vtp.sharedpreference.SpHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import kotlinx.android.synthetic.main.activity_expandable_settings.*

/**
 *
 */
class SettingsActivity : ReturnableActivity() {
    lateinit var adapter: SettingsExpandableAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expandable_settings)

        val expandableListView = expand_list
        adapter = SettingsExpandableAdapter(this, initData(), expandableListView)
        expandableListView.setAdapter(adapter)

        try {
            expandableListView?.post {
                expandableListView.apply {
                    expandGroup(0)
                    expandGroup(1)
                }
            }
        } catch (e: Exception) {
        }
    }

    var first = true
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus && first) {
            startTutorials()
            first = false
        }
    }

    private fun startTutorials() {

    }

    private fun initData(): List<SettingGroupItem> = listOf(
            SettingGroupItem(R.color.indigo_700, titleS = "语音识别", childItems = listOf(
                    SingleChoiceItem(title = "语音引擎", summary = "语音识别/唤醒/合成引擎\n切换后，若需设置语音合成，请重新进入此页面",
                            keyId = cn.vove7.common.R.string.key_speech_engine_type,
                            entityArrId = R.array.list_speech_engine, defaultValue = { AppConfig.speechEngineType }) { o, it ->
                        if (it.first == 1 && !AppLogic.canXunfei()) {
                            GlobalApp.toastInfo("永久会员才可使用讯飞引擎", 1)
                            return@SingleChoiceItem false
                        } else {
                            storeIndexOnSingleChoiceItem(o, it)
                            GlobalApp.toastInfo("切换语音引擎...")
                            ThreadPool.runOnCachePool {
                                MainService.instance?.loadSpeechService(it.first, true)
                            }
                        }
                        false
                    },

                    SwitchItem(title = "长语音模式", summary = "开启后，唤醒后可连续说出命令\n可以通过按音量下键终止\n" +
                            "会占用麦克风",
                            keyId = R.string.key_lasting_voice_command, defaultValue = { AppConfig.lastingVoiceCommand }),

                    CheckBoxItem(R.string.text_voice_control_dialog, summary = "使用语言命令控制对话框",
                            keyId = R.string.key_voice_control_dialog, defaultValue = true)

            )),
            SettingGroupItem(R.color.cyan_500, titleS = "语音唤醒", childItems = listOf(
                    SwitchItem(R.string.text_open_voice_wakeup, summary = "以 \"你好小V\" 唤醒\n提示: 目前语音唤醒十分耗电,请三思" +
                            (if (AppConfig.voiceWakeup && !MainService.wakeupOpen) "\n已自动关闭" else ""),
                            keyId = R.string.key_open_voice_wakeup, callback = { _, it ->
                        when (it) {
                            true -> {
                                AppBus.post(AppBus.ACTION_START_WAKEUP)
                            }
                            false -> AppBus.post(AppBus.ACTION_STOP_WAKEUP)
                        }
                        return@SwitchItem true
                    }, defaultValue = { false }),
                    SingleChoiceItem(title = "自动休眠时长", summary = "在非充电状态下，为了节省电量，在无操作一段时间后将自动关闭唤醒\n默认10分钟",
                            keyId = R.string.key_auto_sleep_wakeup_duration,
                            entityArrId = R.array.list_auto_sleep_duration, defaultValue = { 0 }) { _, _ ->
                        if (AppConfig.voiceWakeup) {
                            AppBus.post(AppBus.ACTION_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY)
                            AppBus.post(AppBus.ACTION_START_WAKEUP_WITHOUT_SWITCH)
                        }
                        return@SingleChoiceItem true
                    },
                    CheckBoxItem(title = "息屏显示通知", summary = "息屏时弹出语音唤醒关闭通知",
                            defaultValue = AppConfig.notifyWpOnScreenOff, keyId = R.string.key_notify_wp_on_screen_off)
                    ,
                    CheckBoxItem(title = "亮屏开启唤醒", summary = "自动休眠后，开屏自动打开语音唤醒",
                            keyId = R.string.key_open_voice_wakeup_if_auto_sleep, defaultValue = true),

//                    CheckBoxItem(title = "熄屏按键唤醒", summary = "熄屏时仍开启按键唤醒",
//                            keyId = R.string.key_volume_wakeup_when_screen_off, defaultValue = { true }),

                    CheckBoxItem(R.string.text_auto_open_voice_wakeup_charging, keyId = R.string.key_auto_open_voice_wakeup_charging) { _, b ->
                        if (PowerEventReceiver.isCharging) {//充电中生效
                            if (b) {//正在充电，开启
                                AppBus.post(AppBus.ACTION_START_WAKEUP_WITHOUT_SWITCH)
                            } else if (!AppConfig.voiceWakeup) {
                                AppBus.post(AppBus.ACTION_STOP_WAKEUP_WITHOUT_SWITCH)
                            }
                        }
                        return@CheckBoxItem true
                    },
                    IntentItem(R.string.text_customize_wakeup_words) {
                        MaterialDialog(this).title(R.string.text_customize_wakeup_words)
                                .customView(R.layout.dialog_customize_wakeup_words, scrollable = true)
                                .show {
                                    findViewById<TextView>(R.id.wakeup_file_path).text = "当前文件路径：${AppConfig.wakeUpFilePath}"
                                    findViewById<View>(R.id.get_wakeup_file).setOnClickListener {
                                        SystemBridge.openUrl("https://vove.gitee.io/2018/10/11/GET_WAKEUP_FILE/")
                                        this.dismiss()
                                    }
//                                    neutralButton(text = "恢复默认") {
//                                        setPathAndReload(AppConfig.DEFAULT_WAKEUP_FILE)
//                                    }
                                    positiveButton(text = "你好小V") {
                                        setPathAndReload(AppConfig.WAKEUP_FILE_NHXV)
                                    }
                                    negativeButton(text = "小V同学") {
                                        setPathAndReload(AppConfig.WAKEUP_FILE_XVTX)
                                    }
                                    findViewById<View>(R.id.sel_wakeup_file).setOnClickListener {
                                        val selIntent = Intent(Intent.ACTION_GET_CONTENT)
                                        selIntent.type = "*/*"
                                        selIntent.addCategory(Intent.CATEGORY_OPENABLE)
                                        try {
                                            startActivityForResult(selIntent, 1)
                                            this.dismiss()
                                        } catch (e: ActivityNotFoundException) {
                                            e.printStackTrace()
                                            GlobalApp.toastError(R.string.text_cannot_open_file_manager)
                                        }
                                    }
                                }
                    },
                    InputItem(title = "用户唤醒词", summary = "如果不想把你的唤醒词被当作命令，把他写到这里\n多个词以'#'隔开",
                            keyId = R.string.key_user_wakeup_word)
            )),

            SettingGroupItem(R.color.google_green, titleS = "按键唤醒", childItems = listOf(

                    SwitchItem(title = "音量键唤醒",
                            keyId = R.string.key_long_press_volume_up_wake_up, summary = "可通过长按音量上键唤醒\n需要无障碍模式开启",
                            defaultValue =
                            { AppConfig.isLongPressVolUpWakeUp }),
                    NumberPickerItem(title = "长按延时", summary = "单位: ms", defaultValue =
                    { AppConfig.volumeKeyDelayUp },
                            keyId = R.string.key_long_key_press_delay, range = Pair(200, 1000)),
                    CheckBoxItem(title = "耳机中键唤醒", summary = "长按耳机中键进行唤醒", keyId = R.string.key_wakeup_with_headsethook,
                            defaultValue = AppConfig.wakeUpWithHeadsetHook),
                    IntentItem(R.string.text_add_wakeup_shortcut_to_launcher, summary = "添加需要8.0+\n" +
                            "7.1+可直接在桌面长按图标使用Shortcut快捷唤醒\ntips:桌面可直接添加小部件")
                    {
                        ShortcutUtil.addWakeUpPinShortcut()
                    }
            )),

            SettingGroupItem(R.color.google_green, "反馈", childItems = listOf(
                    SwitchItem(title = "提示音", summary = "语音识别提示音", keyId = R.string.key_voice_recog_feedback, defaultValue = { AppConfig.voiceRecogEffect }),
                    CheckBoxItem(R.string.text_vibrate_reco_begin,
                            keyId = R.string.key_vibrate_reco_begin, defaultValue = true),
                    CheckBoxItem(title = "执行失败", keyId = R.string.key_exec_failed_voice_feedback,
                            summary = "失败时的语音反馈",
                            defaultValue = AppConfig.execFailedVoiceFeedback),
                    CheckBoxItem(title = "执行结束", keyId = R.string.key_exec_succ_feedback,
                            summary = "结束时状态栏效果反馈",
                            defaultValue = AppConfig.execSuccessFeedback)
            )),

            SettingGroupItem(R.color.indigo_700, "响应词", childItems = listOf(
                    SwitchItem(title = "开启", summary = "开始识别前，响应词反馈", keyId = R.string.key_open_response_word,
                            defaultValue =
                            { AppConfig.openResponseWord }),
                    InputItem(title = "设置响应词", summary = AppConfig.responseWord,
                            keyId = R.string.key_response_word, defaultValue =
                    { AppConfig.responseWord }),
                    CheckBoxItem(title = "仅在语音唤醒时响应", keyId = R.string.key_speak_response_word_on_voice_wakeup,
                            defaultValue = AppConfig.speakResponseWordOnVoiceWakeup)
            )),
            SettingGroupItem(R.color.google_yellow, "语音合成", childItems = listOf(
                    SingleChoiceItem(R.string.text_sound_model, summary = "在线声音模型", keyId = R.string.key_voice_syn_model,
                            defaultValue =
                            { 0 }, entityArrId = R.array.voice_model_entities, callback =
                    { h, i ->
                        AppBus.post(AppBus.ACTION_RELOAD_SYN_CONF)
                        return@SingleChoiceItem true
                    }),
                    NumberPickerItem(R.string.text_speak_speed, keyId = R.string.key_voice_syn_speed,
                            defaultValue =
                            { 5 }, range = Pair(1, 9), callback =
                    { h, i ->
                        AppBus.post(AppBus.ACTION_RELOAD_SYN_CONF)
                        return@NumberPickerItem true
                    }),
                    SingleChoiceItem(title = "输出方式", summary = "选择音量跟随\n可能重启App生效", keyId = R.string.key_stream_of_syn_output,
                            entityArrId = R.array.list_stream_syn_output, defaultValue =
                    { 0 })
                    { _, b ->
                        MainService.instance?.speechSynService?.reloadStreamType()
                        return@SingleChoiceItem true
                    }
            )),
            SettingGroupItem(R.color.google_red, titleS = "悬浮面板", childItems = listOf(
                    SingleChoiceItem(title = "动画", entityArrId = R.array.list_fp_animation, keyId = R.string.key_fp_animation)
            )),
//            SettingGroupItem(R.color.lime_600, titleS = "语音面板", childItems = listOf(
//                    SingleChoiceItem(title = "依靠方向", keyId = R.string.key_float_voice_align,
//                            entityArrId = R.array.list_float_voice_align) { _, b ->
//                        MainService.instance?.toastAlign = (b as Pair<*, *>).first as Int
//                        return@SingleChoiceItem true
//                    }
//            )),
            SettingGroupItem(R.color.lime_600, titleId = R.string.text_other, childItems = listOf(
                    SingleChoiceItem(title = "翻译主语言", entityArrId = R.array.list_translate_languages,
                            keyId = R.string.key_translate_languages),
                    CheckBoxItem(title = "自动开启无障碍服务", summary = "App启动时自动开启无障碍服务，需要root支持，或者转为系统应用",
                            keyId = R.string.key_auto_open_as_with_root, defaultValue = false)
                    { _, b ->
                        if (b) ThreadPool.runOnPool {
                            if (AccessibilityApi.isBaseServiceOn)
                                return@runOnPool
                            RootHelper.openSelfAccessService()
                        }
                        return@CheckBoxItem true
                    },
                    IntentItem(title = "重置引导") {
                        Tutorials.resetTutorials()
                        GlobalApp.toastInfo("重置完成")
                    },
                    CheckBoxItem(title = "用户体验计划", summary = "改善体验与完善功能",
                            keyId = R.string.key_user_exp_plan, defaultValue = true
                    )
            ))
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {//选择文件回调
            when (requestCode) {
                1 -> {
                    val uri = data?.data
                    if (uri != null) {
                        try {
                            val path = UriUtils.getPathFromUri(this, uri)
                            when {
                                path == null -> GlobalApp.toastError("路径获取失败")
                                path.endsWith(".bin") -> setPathAndReload(path)
                                else -> GlobalApp.toastInfo("请选择.bin文件")
                            }
                        } catch (e: Exception) {
                            GlobalLog.err(e)
                            GlobalApp.toastError("设置失败")
                        }
                    } else {
                        GlobalApp.toastError(getString(R.string.text_open_failed))
                    }
                }
                else -> {
                }
            }
        }
    }

    private fun setPathAndReload(path: String) {
        SpHelper(this).set(R.string.key_wakeup_file_path, path)
        AppConfig.reload()
        if (AppConfig.voiceWakeup) {
            GlobalApp.toastInfo("正在重载配置")
            Handler().postDelayed({
                AppBus.post(AppBus.ACTION_STOP_WAKEUP_WITHOUT_SWITCH)
                Thread.sleep(2000)
                AppBus.post(AppBus.ACTION_START_WAKEUP_WITHOUT_SWITCH)
            }, 1000)
        } else GlobalApp.toastSuccess("设置完成")
    }

}
