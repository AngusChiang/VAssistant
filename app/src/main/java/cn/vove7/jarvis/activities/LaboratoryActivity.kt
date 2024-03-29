package cn.vove7.jarvis.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.title
import cn.vove7.bottomdialog.extension.awesomeHeader
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.helper.ConnectiveNsdHelper
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.content
import cn.vove7.common.utils.runOnUiDelay
import cn.vove7.common.utils.startActivity
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.adapters.SettingsExpandableAdapter
import cn.vove7.jarvis.databinding.ActivityExpandableSettingsBinding
import cn.vove7.jarvis.plugins.*
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.debugserver.ConnectiveService
import cn.vove7.jarvis.view.*
import cn.vove7.jarvis.view.custom.SettingGroupItem
import cn.vove7.jarvis.view.dialog.TextEditorDialog
import cn.vove7.jarvis.view.dialog.contentbuilder.SettingItemBuilder
import cn.vove7.jarvis.view.dialog.contentbuilder.markdownContent
import cn.vove7.jarvis.view.dialog.editorView
import cn.vove7.vtp.extend.buildList
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * # LaboratoryActivity
 *
 * @author Administrator
 * 9/24/2018
 */
class LaboratoryActivity : ReturnableActivity<ActivityExpandableSettingsBinding>() {
    override val darkTheme: Int
        get() = R.style.DarkTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val expandableListView = viewBinding.expandList
        val adapter = SettingsExpandableAdapter(this, groupItems, expandableListView)
        expandableListView.setAdapter(adapter)

        expandableListView.post {
            expandableListView.apply {
                expandGroup(0)
                expandGroup(2)
            }
        }
    }

    private val groupItems: List<SettingGroupItem> by lazy {
        listOf(SettingGroupItem(
            R.color.indigo_700, "扩展",
            childItems = listOf(
                CheckBoxItem(title = "电源提醒", keyId = R.string.key_extension_power_indicator, onTileAreaClick = {
                    showExtensionSettings("电源提醒", mutableListOf(
                        InputItem(
                            title = "满电",
                            keyId = R.string.key_full_power_hint_text,
                            defaultValue = { PluginConfig.onFullText }
                        ),
                        InputItem(
                            title = "低电量",
                            keyId = R.string.key_low_power_hint_text,
                            defaultValue = { PluginConfig.onLowText }
                        ),
                        InputItem(
                            title = "开始充电",
                            keyId = R.string.key_charging_hint_text,
                            defaultValue = { PluginConfig.onChargingText }
                        )
                    ))
                }, defaultValue = false) { _, d ->
                    if (d) PowerListener.start()
                    else PowerListener.stop()
                    return@CheckBoxItem true
                },
                CheckBoxItem(
                    title = "去广告服务",
                    summary = (if (UserInfo.isVip()) "" else "非高级用户，一天最多去广告5次\n") + "需要无障碍权限",
                    keyId = R.string.key_open_ad_block,
                    defaultValue = AppConfig.isAdBlockService,
                    onTileAreaClick = {
                        showExtensionSettings("去广告服务", mutableListOf(
                            NumberPickerItem(R.string.text_time_wait_ad, "界面等待广告出现最长时间，单位秒",
                                keyId = R.string.key_ad_wait_secs, range = Pair(10, 100),
                                defaultValue = { PluginConfig.adWaitSecs }),
                            CheckBoxItem(title = "智能识别广告", summary = "[应用切换]时识别未标记的广告页并清除\n有效时间1.5s\n可能会增加耗电",
                                keyId = R.string.key_smart_find_and_kill_ad, defaultValue = PluginConfig.smartKillAd),
                            CheckBoxItem(R.string.text_show_toast_when_remove_ad, summary = getString(R.string.text_show_toast_when_remove_ad_summary), keyId = R.string.key_show_toast_when_remove_ad, defaultValue = true)
                        ))
                    }
                ) { _, it ->
                    when (it) {
                        true -> AdKillerService.register()
                        false -> AdKillerService.unregister()
                    }
                    return@CheckBoxItem true
                },
                IntentItem(title = "定时任务", summary = "定时执行语音指令或脚本") {
                    startActivity<TimedTaskManagerActivity>()
                },
                CheckBoxItem(
                    title = "地铁WiFi",
                    summary = "自动登录地铁WiFi",
                    keyId = R.string.key_auto_login_metro_wlan,
                    defaultValue = AppConfig.autoLoginMetroWlan,
                ) { _, d ->
                    if (d) MetroWlanListener.start()
                    else MetroWlanListener.stop()
                    return@CheckBoxItem true
                },
            ),
        ),
            SettingGroupItem(R.color.google_red, titleS = "屏幕助手", childItems = listOf(
                SwitchItem(title = "助手模式", summary = "设为默认语音辅助应用后\n通过唤醒用系统语音助手触发\n可捕捉屏幕内容进行快捷操作\n关闭后只能使快速唤醒", keyId = R.string.key_use_assist_service,
                    defaultValue = AppConfig.useAssistService),
                InputItem(title = "文字识别参数设置", summary = "自定义文字识别key", keyId = R.string.key_text_ocr_key),
                SingleChoiceItem(title = "长按HOME键操作", summary = "适用于一加",
                    keyId = R.string.key_home_fun, entityArrId = R.array.list_home_funs)
            )),
            SettingGroupItem(R.color.google_yellow, titleS = "互联服务", childItems = mutableListOf(
                SwitchItem(
                    title = "开启",
                    summary = "开启后可在同一局域网内多台设备间传递指令",
                    keyId = R.string.key_connective_service,
                    defaultValue = AppConfig.connectiveService
                ) { _, it ->
                    if (it) {
                        ConnectiveService.start()
                    } else {
                        ConnectiveService.stop()
                    }
                    true
                },
                InputItem(
                    title = "设备名",
                    defaultValue = { AppConfig.deviceName },
                    clearable = false
                ),
                IntentItem(title = "帮助") {
                    BottomDialog.builder(this) {
                        awesomeHeader("帮助 - 互联服务")
                        markdownContent {
                            loadMarkdownFromAsset("files/connective-service.md")
                        }
                    }
                },
                IntentItem(title = "扫描测试") {
                    showNsdDevice()
                }
            ).also {
                if (BuildConfig.DEBUG) {
                    it += IntentItem(title = "测试发送") {
                        launch {
                            SystemBridge.sendCommand2OtherDevices("你好")
                        }
                    }
                    it += IntentItem(title = "发送指令->Self") {
                        SystemBridge.sendCommand2RemoteDevice(listOf(SystemBridge.getLocalIpAddress()!! to "Self"), "你好")
                    }
                    it += IntentItem(title = "发送脚本->Self") {
                        SystemBridge.sendScript2RemoteDevice(listOf(SystemBridge.getLocalIpAddress()!! to "Self"), "toast('from bus')", "lua")
                    }
                }
            }),
            SettingGroupItem(R.color.a8nv, titleS = "智能家居", childItems = listOf(
                SingleChoiceItem(title = "智能家居系统", summary = "选择您的家居系统",
                    defaultValue = AppConfig.homeSystem ?: -1,
                    keyId = R.string.key_home_system,
                    items = buildList {
                        if (AppConfig.netConfig("rokid", true)) {
                            this += "Rokid(若琪)"
                        }
                    }, allowClear = true
                ) { _, data ->
                    MainService.loadHomeSystem(data?.first)
                    true
                },
                IntentItem(title = "系统设置") {
                    val hs = MainService.homeControlSystem
                    if (hs != null) {
                        val settings = hs.getSettingItems(this)
                        if (settings.isNotEmpty()) {
                            showExtensionSettings(hs.name + "设置", settings.toMutableList())
                        } else {
                            GlobalApp.toastInfo("该家居系统无设置")
                        }
                    } else {
                        GlobalApp.toastInfo("请选择家居系统")
                    }
                },
                IntentItem(title = "自定义短语", summary = "自定义发送到家居控制系统的短语") {
                    TextEditorDialog(this, AppConfig.homeSystemUserCommand) {
                        title(text = "自定义短语")
                        editorView.hint = "每行一个"
                        editorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                        positiveButton(text = "保存") {
                            val text = editorView.content()
                            AppConfig.homeSystemUserCommand = text
                            MainService.homeControlSystem?.loadUserCommand()
                            GlobalApp.toastSuccess("保存完成")
                        }
                        negativeButton()
                    }
                }
            )),
            SettingGroupItem(R.color.google_green, titleS = "聊天", childItems = listOf(
                SwitchItem(title = "开启", summary = "指令匹配失败，调用聊天系统",
                    keyId = R.string.key_open_chat_system, defaultValue = true) { _, b ->
                    if (b) {
                        MainService.loadChatSystem()
                    }
                    return@SwitchItem true
                },
                SingleChoiceItem(title = "对话系统",
                    keyId = R.string.key_chat_system_type, entityArrId = R.array.list_chat_system,
                    defaultValue = 0) { _, d ->
                    launch {
                        delay(800)//等待设置完成
                        MainService.loadChatSystem()
                        GlobalApp.toastInfo("对话系统切换完成")
                    }
                    return@SingleChoiceItem true
                },
                InputItem(title = "自定义参数", keyId = R.string.key_chat_str),
                IntentItem(title = "自定义教程") {
                    SystemBridge.openUrl("https://vove.gitee.io/2019/01/24/custom_chat_system/")
                }

            )),
            SettingGroupItem(R.color.deep_purple_600, titleS = "语音唤醒", childItems = listOf(
                SwitchItem(title = "自动释放麦克风", summary = "在已授予麦克风权限的其他App内自动关闭语音唤醒\n需要无障碍",/*设为系统应用后无效*/
                    keyId = R.string.key_fix_voice_micro, defaultValue = AppConfig.fixVoiceMicro) { _, b ->
                    if (b /* TODO && !AppConfig.IS_SYS_APP*/) {
                        VoiceWakeupStrategy.register()
                    } else {
                        VoiceWakeupStrategy.unregister()
                    }
                    return@SwitchItem true
                },
                CheckBoxItem(title = "显示通知", summary = "关闭和打开时在状态栏显示通知",
                    keyId = R.string.key_close_wakeup_notification, defaultValue = true)

            ))
        )
    }

    @SuppressLint("CheckResult")
    private fun showNsdDevice() {
        var ds = SystemBridge.scanVAssistHostsInLAN()
        fun MaterialDialog.refresh() = apply {
            title(text = "互联设备(数量：${ds.size})")
            listItems(items = ds.map {
                it.second + " / " + it.first
            })
        }
        MaterialDialog(this)
            .noAutoDismiss()
            .cancelable(false)
            .positiveButton(text = "刷新") {
                ds = emptyList()
                it.refresh()
                it.title(text = "正在刷新...")
                lifecycleScope.launch {
                    ConnectiveNsdHelper.reset()
                    runOnUiDelay(3000) {
                        ds = SystemBridge.scanVAssistHostsInLAN()
                        it.refresh()
                    }
                }
            }
            .negativeButton(text = "完成") {
                it.dismiss()
            }
            .refresh()
            .show()
    }

    private fun showExtensionSettings(
        title: String,
        items: MutableList<SettingChildItem>
    ) = BottomDialog.builder(this) {
        peekHeightProportion = 0.6f
        expandable = false
        title(title, round = true, centerTitle = true)
        content(SettingItemBuilder(this@LaboratoryActivity, items, PluginConfig))
    }

}
