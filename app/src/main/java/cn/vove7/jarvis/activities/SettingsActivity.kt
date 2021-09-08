package cn.vove7.jarvis.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.widget.BaseAdapter
import android.widget.TextView
import cn.vove7.android.common.logi
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.runOnUiDelay
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.adapters.SettingsExpandableAdapter
import cn.vove7.jarvis.databinding.ActivityExpandableSettingsBinding
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.services.MyAccessibilityService
import cn.vove7.jarvis.tools.ShortcutUtil
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.tools.UriUtils
import cn.vove7.jarvis.tools.checkBoxText
import cn.vove7.jarvis.view.*
import cn.vove7.jarvis.view.custom.SettingGroupItem
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

/**
 *
 */
class SettingsActivity : ReturnableActivity<ActivityExpandableSettingsBinding>() {
    override val darkTheme: Int
        get() = R.style.DarkTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val expandableListView = viewBinding.expandList
        expandableListView.setAdapter(SettingsExpandableAdapter(this, initData {
            expandableListView.adapter as BaseAdapter
        }, expandableListView))

        expandableListView.post {
            kotlin.runCatching {
                expandableListView.apply {
                    expandGroup(0)
                    expandGroup(2)
                }
            }
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

    private fun initData(getAdapter: () -> BaseAdapter): List<SettingGroupItem> = listOf(
        SettingGroupItem(R.color.indigo_700, titleS = "语音识别", childItems = listOf(
//                    SingleChoiceItem(title = "语音引擎", summary = "语音识别/唤醒/合成引擎\n切换后，若需设置语音合成，请重新进入此页面",
//                            keyId = cn.vove7.common.R.string.key_speech_engine_type,
//                            entityArrId = R.array.list_speech_engine, defaultValue = { AppConfig.speechEngineType }) { o, it ->
//                        if (it.first == 1 && !AppLogic.canXunfei()) {
//                            GlobalApp.toastInfo("永久会员才可使用讯飞引擎", 1)
//                            return@SingleChoiceItem false
//                        } else {
//                            storeIndexOnSingleChoiceItem(o, it)
//                            GlobalApp.toastInfo("切换语音引擎...")
//                            ThreadPool.runOnCachePool {
//                                MainService.loadSpeechService(it.first, true)
//                            }
//                        }
//                        false
//                    },

            SwitchItem(
                title = "连续对话",
                summary = "开启后，唤醒后可连续说出命令\n可以通过按音量下键终止",
                keyId = R.string.key_lasting_voice_command,
                defaultValue = AppConfig.lastingVoiceCommand
            ),
            NumberPickerItem(
                title = "结束等待时长",
                keyId = R.string.key_recog_wait_duration,
                summary = "在未识别到声音后自动结束识别（单位ms）",
                defaultValue = { AppConfig.recogWaitDurationMillis },
                range = 300 to 3000),
            CheckBoxItem(
                R.string.text_voice_control_dialog,
                summary = "使用语言命令控制对话框",
                keyId = R.string.key_voice_control_dialog,
                defaultValue = true
            ),
            CheckBoxItem(
                title = "蓝牙设备支持",
                summary = "可能某些蓝牙设备无效",
                keyId = R.string.key_bt_support,
                defaultValue = true
            ),
            CheckBoxItem(
                title = "离线支持",
                summary = "在出现10012错误时请关闭",
                keyId = R.string.key_baidu_enable_offline,
                defaultValue = true
            ),
        )),
        SettingGroupItem(R.color.cyan_500, titleS = "语音唤醒", childItems = listOf(
            SwitchItem(
                R.string.text_open_voice_wakeup,
                summary = "以 \"你好小V\" 唤醒\n提示: 目前语音唤醒十分耗电,请三思" +
                    (if (AppConfig.voiceWakeup && !MainService.wakeupOpen) "\n已自动关闭" else ""),
                keyId = R.string.key_open_voice_wakeup,
                callback = { _, it ->
                    when (it) {
                        true -> {
                            AppBus.post(AppBus.ACTION_START_WAKEUP)
                        }
                        false -> AppBus.post(AppBus.ACTION_STOP_WAKEUP)
                    }
                    return@SwitchItem true
                },
                defaultValue = false
            ),
            SingleChoiceItem(
                title = "自动休眠时长",
                summary = "在非充电状态下，为了节省电量，在无操作一段时间后将自动关闭唤醒\n默认10分钟",
                keyId = R.string.key_auto_sleep_wakeup_duration,
                entityArrId = R.array.list_auto_sleep_duration,
                defaultValue = 0
            ) { _, _ ->
                if (AppConfig.voiceWakeup) {
                    AppBus.post(AppBus.ACTION_STOP_VOICE_WAKEUP_WITHOUT_NOTIFY)
                    AppBus.post(AppBus.ACTION_START_WAKEUP_WITHOUT_SWITCH)
                }
                return@SingleChoiceItem true
            },
            CheckBoxItem(
                title = "息屏显示通知",
                summary = "息屏时弹出语音唤醒关闭通知",
                defaultValue = AppConfig.notifyWpOnScreenOff,
                keyId = R.string.key_notify_wp_on_screen_off
            ),
            CheckBoxItem(
                title = "亮屏开启唤醒",
                summary = "自动休眠后，开屏自动打开语音唤醒",
                keyId = R.string.key_open_voice_wakeup_if_auto_sleep,
                defaultValue = true
            ),

//                    CheckBoxItem(title = "熄屏按键唤醒", summary = "熄屏时仍开启按键唤醒",
//                            keyId = R.string.key_volume_wakeup_when_screen_off, defaultValue = { true }),

            CheckBoxItem(
                R.string.text_auto_open_voice_wakeup_charging,
                keyId = R.string.key_auto_open_voice_wakeup_charging
            ) { _, b ->
                if (PowerEventReceiver.isCharging) {//充电中生效
                    if (b) {//正在充电，开启
                        AppBus.post(AppBus.ACTION_START_WAKEUP_WITHOUT_SWITCH)
                    } else if (!AppConfig.voiceWakeup) {
                        AppBus.post(AppBus.ACTION_STOP_WAKEUP_WITHOUT_SWITCH)
                    }
                }
                return@CheckBoxItem true
            },
            CheckBoxItem(
                title = "自动亮屏",
                summary = "在息屏语音唤醒时点亮屏幕",
                keyId = R.string.key_wakeup_screen_when_vw,
                defaultValue = AppConfig.wakeupScreenWhenVw
            ),
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
            InputItem(
                title = "用户唤醒词",
                summary = "如果不想把你的唤醒词被当作命令，把他写到这里\n多个词以'#'隔开",
                keyId = R.string.key_user_wakeup_word
            )
        )),

        SettingGroupItem(R.color.google_green, titleS = "按键唤醒", childItems = listOf(
            SwitchItem(
                title = "长按唤醒",
                keyId = R.string.key_long_press_volume_up_wake_up,
                summary = "可通过长按[音量上键和自定义键]唤醒\n需要无障碍模式开启",
                defaultValue = AppConfig.isLongPressKeyWakeUp
            ),
            NumberPickerItem(
                title = "长按延时",
                summary = "单位: ms",
                defaultValue =
                { AppConfig.volumeKeyDelayUp },
                keyId = R.string.key_long_key_press_delay,
                range = 200 to 1000
            ),
            CheckBoxItem(
                title = "耳机中键唤醒",
                summary = "长按耳机中键进行唤醒",
                keyId = R.string.key_wakeup_with_headsethook,
                defaultValue = AppConfig.wakeUpWithHeadsetHook
            ),
            IntentItem(
                title = "自定义按键",
                summary = AppConfig.wakeupKeys.let {
                    if (it.isEmpty()) "未设置"
                    else it.contentToString()
                }
            ) {
                if (AccessibilityApi.isBaseServiceOn) {
                    listenKeyDialog()
                } else {
                    GlobalApp.toastInfo("请先开启无障碍服务")
                }
            },
            IntentItem(R.string.text_add_wakeup_shortcut_to_launcher, summary = "添加需要8.0+\n" +
                "7.1+可直接在桌面长按图标使用Shortcut快捷唤醒\ntips:桌面可直接添加小部件")
            {
                ShortcutUtil.addWakeUpPinShortcut()
            }
        )),

        SettingGroupItem(R.color.amber_A700, "反馈", childItems = listOf(
            SwitchItem(
                title = "提示音",
                summary = "语音识别提示音",
                keyId = R.string.key_voice_recog_feedback,
                defaultValue = AppConfig.voiceRecogEffect
            ),
            CheckBoxItem(
                R.string.text_vibrate_reco_begin,
                keyId = R.string.key_vibrate_reco_begin,
                defaultValue = AppConfig.vibrateWhenStartRecog
            ),
            NumberPickerItem(
                title = "震动效果",
                range = 20..100,
                defaultValue = { AppConfig.vibrateEffectStartRecog },
                keyId = R.string.key_vibrate_reco_effect,
                onChange = {
                    SystemBridge.vibrate(it.toLong())
                }
            ),
            CheckBoxItem(title = "连接蓝牙时播放提示音", keyId = R.string.key_voice_recog_feedback_bt,
                summary = "即使关闭提示音",
                defaultValue = AppConfig.voiceRecogWhenBt
            )
//                    CheckBoxItem(title = "执行失败", keyId = R.string.key_exec_failed_voice_feedback,
//                            summary = "失败时的语音反馈",
//                            defaultValue = AppConfig.execFailedVoiceFeedback),
//                    CheckBoxItem(title = "执行结束", keyId = R.string.key_exec_succ_feedback,
//                            summary = "结束时状态栏效果反馈",
//                            defaultValue = AppConfig.execSuccessFeedback)
        )),

        SettingGroupItem(R.color.indigo_700, "响应词", childItems = listOf(
            SwitchItem(
                title = "开启",
                summary = "开始识别前，响应词反馈",
                keyId = R.string.key_open_response_word,
                defaultValue = AppConfig.openResponseWord
            ),
            InputItem(
                title = "设置响应词",
                summary = AppConfig.responseWord,
                keyId = R.string.key_response_word,
                defaultValue =
                { AppConfig.responseWord }
            ),
            CheckBoxItem(
                title = "仅在语音唤醒时响应",
                keyId = R.string.key_speak_response_word_on_voice_wakeup,
                defaultValue = AppConfig.speakResponseWordOnVoiceWakeup
            )
        )),
        SettingGroupItem(R.color.google_yellow, "语音合成", childItems = listOf(
            SingleChoiceItem(title = "语音合成引擎", summary = "语音合成引擎",
                keyId = cn.vove7.common.R.string.key_speech_syn_type,
                entityArrId = R.array.list_speech_syn_engine,
                defaultValue = AppConfig.speechSynType) { o, it ->
                it ?: return@SingleChoiceItem false
                GlobalApp.toastInfo("切换语音引擎...")
                GlobalScope.launch {
                    MainService.loadSpeechService(synType = it.first, notify = true)
                }
                runOnUiDelay(800) {
                    getAdapter().notifyDataSetChanged()
                }
                true
            },
            SingleChoiceItem(
                R.string.text_sound_model,
                summary = "在线声音模型",
                keyId = R.string.key_voice_syn_model,
                defaultValue = 0,
                entityArrId = R.array.voice_model_entities,
                enabled = { AppConfig.speechSynType == 0 }
            ) { h, i ->
                AppBus.postDelay(AppBus.ACTION_RELOAD_SYN_CONF, 500)
                return@SingleChoiceItem true
            },
            NumberPickerItem(
                R.string.text_speak_speed,
                keyId = R.string.key_voice_syn_speed,
                defaultValue =
                { 5 },
                range = 1 to 9,
                enabled = { AppConfig.speechSynType == 0 }
            ) { h, i ->
                AppBus.post(AppBus.ACTION_RELOAD_SYN_CONF)
                return@NumberPickerItem true
            },
            SingleChoiceItem(
                title = "音量输出",
                summary = "选择音量跟随\n可能重启App生效",
                keyId = R.string.key_stream_of_syn_output,
                entityArrId = R.array.list_stream_syn_output,
                defaultValue = 0,
                enabled = { AppConfig.speechSynType == 0 }
            ) { _, b ->
                AppBus.postDelay(AppBus.ACTION_RELOAD_SYN_CONF, 500)
//                        MainService.speechSynService?.reloadStreamType()
                return@SingleChoiceItem true
            },
            IntentItem(title = "试听") {
                MainService.speak(
                    if (AppConfig.speechSynType == 0)
                        "百度语音：基于业界领先的深度神经网络技术。"
                    else "语音合成测试"
                )
            }
        )),
        SettingGroupItem(R.color.google_red, titleS = "悬浮面板", childItems = listOf(
            SingleChoiceItem(
                title = "面板样式",
                keyId = R.string.key_panel_style,
                entityArrId = R.array.list_panel_style,
                defaultValue = 0
            ) { _, p ->
                p ?: return@SingleChoiceItem false
                if (p.first != AppConfig.panelStyle) {
                    MainService.loadFloatPanel(p.first)
                }
                true
            },
            IntentItem(title = "面板设置") {
                MainService.showPanelSettings(this)
            }
        )),
        SettingGroupItem(R.color.a81nv, titleS = "启动选项", childItems = listOf(
            CheckBoxItem(
                title = "自动开启无障碍服务",
                summary = "App启动时自动开启无障碍服务\n" +
                    "需要ROOT支持，或者使用ADB授予WRITE_SECURE_SETTINGS权限（方法见常见问题）",
                keyId = R.string.key_auto_open_as_with_root,
                defaultValue = AppConfig.autoOpenAS
            ) { _, b ->
                if (b) launch {
                    if (!AccessibilityApi.isBaseServiceOn) {
                        AccessibilityApi.openServiceSelf(0)
                    }
                }
                return@CheckBoxItem true
            },
            CheckBoxItem(
                title = "自动开启高级无障碍服务",
                summary = "App启动时自动开启高级无障碍服务\n需要权限同上",
                keyId = R.string.key_auto_open_aas_with_root,
                defaultValue = AppConfig.autoOpenAAS
            ) { _, b ->
                if (b) launch {
                    if (!AccessibilityApi.isGestureServiceOn) {
                        AccessibilityApi.openServiceSelf(1)
                    }
                }
                return@CheckBoxItem true
            },
            CheckBoxItem(
                title = "自动设为助手应用",
                summary = "App启动时自动设为助手应用\n需要权限同上",
                keyId = R.string.key_auto_set_assistant_app,
                defaultValue = AppConfig.autoSetAssistantApp
            )
        )),
        SettingGroupItem(R.color.lime_600, titleId = R.string.text_other, childItems = listOf(
            SingleChoiceItem(title = "翻译主语言", entityArrId = R.array.list_translate_languages,
                keyId = R.string.key_translate_languages, defaultValue = 0),
            CheckBoxItem(title = "以兼容模式启动应用", summary = "某些机型在外部无法打开其他软件，请尝试开启",
                keyId = R.string.key_open_app_compat, defaultValue = false
            ),
            CheckBoxItem(title = "优先通过ADB执行手势", summary = "执行手势命令优先通过ADB来实现，如果可用",
                keyId = R.string.key_gesture_adb_first, defaultValue = false
            ),
            CheckBoxItem(title = "无障碍按钮", summary = "显示导航栏无障碍按钮，要求系统版本Android O\n动作受[实验室/屏幕助手/长按HOME键操作]控制",
                keyId = R.string.key_show_access_nav_button, defaultValue = AppConfig.showAccessNavButton
            ) { _, b ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    GlobalApp.toastError("要求系统版本Android O")
                    return@CheckBoxItem false
                }
                if (AccessibilityApi.isBaseServiceOn) {
                    val service = (AccessibilityApi.accessibilityService as MyAccessibilityService)
                    if (b) {
                        service.showNavButton()
                    } else {
                        service.hideNavButton()
                    }
                }
                return@CheckBoxItem true
            },
            IntentItem(title = "重置引导") {
                Tutorials.resetTutorials()
                GlobalApp.toastInfo("重置完成")
            },
            CheckBoxItem(title = "用户体验计划", summary = "改善体验与完善功能",
                keyId = R.string.key_user_exp_plan, defaultValue = true
            ),
            IntentItem(
                title = "添加调试Shortcut",
                summary = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) "您的设备不支持" else null
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutUtil.addDebugShortcut()
                }
            }
        ))
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {//选择文件回调
            val uri = data?.data
            if (uri != null) {
                uri.logi()
                try {
                    val path = UriUtils.getPathFromUri(this, uri)
                    when {
                        path == null -> GlobalApp.toastError("路径获取失败")
                        path.endsWith(".bin") -> setPathAndReload(path)
                        else -> GlobalApp.toastInfo("请选择.bin文件")
                    }
                } catch (e: Exception) {
                    GlobalLog.err(e)
                    GlobalApp.toastError("设置失败, 请尝试使用其他文件管理器选择文件")
                }
            } else {
                GlobalApp.toastError(getString(R.string.text_open_failed))
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    var lisKeyDialog: MaterialDialog? = null

    @SuppressLint("CheckResult")
    private fun listenKeyDialog() {
        val service = (AccessibilityApi.accessibilityService as MyAccessibilityService)

        var keyCode: Int? = null

        lisKeyDialog = MaterialDialog(this).show {
            title(text = "自定义按键")
            message(text = "按下某键，此处显示结果")
            checkBoxText(text = "不支持某些按键：电源键，音量键，耳机中键\n注意：某些按键会导致原有功能失效")
            cancelable(false)
            noAutoDismiss()
            positiveButton {
                val k = keyCode
                if (k == null) {
                    GlobalApp.toastInfo("未监听到任何按键")
                } else {
                    //此版仅自定义一个key
                    AppConfig.wakeupKeys = intArrayOf(k)
                    GlobalApp.toastSuccess("设置成功")
                    dismiss()
                }
            }
            negativeButton {
                dismiss()
            }
            neutralButton(text = "清除设置") {
                AppConfig.wakeupKeys = intArrayOf()
                GlobalApp.toastSuccess("设置成功")
                dismiss()
            }

            onDismiss {
                service.keyListener = null
                lisKeyDialog = null
            }
            onCancel {
                service.keyListener = null
                lisKeyDialog = null
            }
        }
        val notSupportKey = arrayOf(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_HEADSETHOOK)
        service.keyListener = {
            if (it.keyCode in notSupportKey) {
                lisKeyDialog?.message(text = "不支持音量键和耳机中键，请在设置中开启")
                keyCode = null
            } else {
                keyCode = it.keyCode
                lisKeyDialog?.message(text = it.keyCode.toString())
            }
        }
    }

    override fun onStop() {
        super.onStop()
        lisKeyDialog?.also {
            it.dismiss()
            (AccessibilityApi.accessibilityService as MyAccessibilityService?)?.keyListener = null
        }
    }

    private fun setPathAndReload(path: String) {
        AppConfig.wakeUpFilePath = if (!path.startsWith("asset")) {
            val userFile = File(filesDir, "user_wakeup.bin")
            File(path).copyTo(userFile, true)
            userFile.absolutePath
        } else {
            path
        }
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
