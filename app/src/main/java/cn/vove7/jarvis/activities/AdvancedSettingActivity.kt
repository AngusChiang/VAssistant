package cn.vove7.jarvis.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.widget.TextView
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.net.WrapperNetHelper
import cn.vove7.common.net.model.LastDateInfo
import cn.vove7.common.utils.startActivity
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.activities.screenassistant.QrCodeActivity
import cn.vove7.jarvis.activities.screenassistant.ScreenOcrActivity
import cn.vove7.jarvis.activities.screenassistant.ScreenShareActivity
import cn.vove7.jarvis.activities.screenassistant.SpotScreenActivity
import cn.vove7.jarvis.adapters.SettingsExpandableAdapter
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.*
import cn.vove7.jarvis.tools.backup.BackupHelper
import cn.vove7.jarvis.tools.debugserver.RemoteDebugServer
import cn.vove7.jarvis.view.CheckBoxItem
import cn.vove7.jarvis.view.InputItem
import cn.vove7.jarvis.view.IntentItem
import cn.vove7.jarvis.view.SwitchItem
import cn.vove7.jarvis.view.custom.SettingGroupItem
import cn.vove7.jarvis.view.dialog.ProgressDialog
import cn.vove7.vtp.app.AppHelper
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import cn.vove7.vtp.view.span.ColourTextClickableSpan
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import kotlinx.android.synthetic.main.activity_expandable_settings.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * # AdvancedSettingActivity
 *
 * @author 17719247306
 * 2018/9/10
 */
class AdvancedSettingActivity : ReturnableActivity() {

    lateinit var adapter: SettingsExpandableAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expandable_settings)
        val expandableListView = expand_list
        adapter = SettingsExpandableAdapter(this, groupItems, expandableListView)

        expandableListView?.setAdapter(adapter)

        expandableListView?.post {
            expandableListView.apply {
                expandGroup(0)
                expandGroup(2)
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
        Handler().postDelayed({
            Tutorials.oneStep(this, list = arrayOf(
                    ItemWrap(Tutorials.t_inst_man, adapter.childHolders[0][0]?.titleView, "指令管理", "这里查看支持的指令和指令管理")
                    , ItemWrap(Tutorials.t_mark_man, adapter.childHolders[0][1]?.titleView, "标记管理", "这里查看标记的数据和管理")
            ))
        }, 1000)
    }

    private val groupItems: List<SettingGroupItem> by lazy {
        mutableListOf(
                SettingGroupItem(R.color.google_blue, "管理", childItems = listOf(
                        IntentItem(R.string.instru_management) {
                            startActivity<InstManagerActivity>()
                        },
                        IntentItem(R.string.text_mark_management) {
                            startActivity<MarkedManagerActivity>()
                        },
                        IntentItem(R.string.text_check_last_data) {
                            showLastDataDate()
                        },
                        CheckBoxItem(title = "自动更新", summary = "在进入App后自动检查并更新最新数据",
                                keyId = R.string.key_auto_update_data, defaultValue = true)
                )),
                SettingGroupItem(R.color.google_green, "脚本", childItems = listOf(
                        SwitchItem(R.string.text_remote_debug, summary = if (RemoteDebugServer.stopped) "使用Pc调试，请查阅使用手册" else ipText, defaultValue = !RemoteDebugServer.stopped, callback = { item, it ->
                            if (!BuildConfig.DEBUG && !AppConfig.checkLogin()) {
                                item.isChecked = false
                                return@SwitchItem true
                            }
                            if (it) {
                                RemoteDebugServer.start()
                                item.summary = ipText
                            } else RemoteDebugServer.stop()
                            return@SwitchItem true
                        }),
                        IntentItem(title = "远程调试示例") {
                            SystemBridge.openUrl("https://vove.gitee.io/2018/09/29/App_Debugger/")
                        },
                        IntentItem(R.string.text_test_code_lua, onClick = {
                            if (BuildConfig.DEBUG || AppConfig.checkLogin()) {
                                startActivity<LuaEditorActivity>()
                            }
                        }),
                        IntentItem(R.string.text_code_test_js, null, onClick = {
                            if (BuildConfig.DEBUG || AppConfig.checkLogin())
                                startActivity<JsEditorActivity>()
                        })
                )),
                SettingGroupItem(R.color.google_red, "备份", childItems = listOf(
                        IntentItem(title = "本机数据备份", summary = "备份用户指令、标记等数据") {
                            if (AppConfig.checkLogin()) {
                                BackupHelper.showBackupDialog(this)
                            }
                        },
                        IntentItem(title = "从本地恢复") {
                            if (UserInfo.isLogin()) {
                                BackupHelper.showBackupFileList(this)
                            } else {
                                GlobalApp.toastInfo("请登录后操作")
                            }
                        },
                        IntentItem(title = "查看云端备份") {
                            //todo
                            (R.string.text_coming_soon)
                        },
                        IntentItem(title = "备份设置", summary = "将设置备份到sd卡") {
                            BackupHelper.backupAppConfig().also {
                                if (it) {
                                    GlobalApp.toastSuccess("备份完成")
                                } else {
                                    GlobalApp.toastError("备份失败，详情见日志")
                                }
                            }
                        },
                        IntentItem(title = "恢复设置", summary = "从sd卡恢复设置\n需重启App") {
                            BackupHelper.restoreAppConfig().also {
                                GlobalApp.toastInfo(it.second)
                                if (it.first) { //跳转重启
                                    GlobalApp.toastSuccess(it.second)
                                    AppHelper.showPackageDetail(this, packageName)
                                } else {
                                    GlobalApp.toastError(it.second)
                                }
                            }
                        }
                )),
                SettingGroupItem(R.color.teal_A700, "命令解析", childItems = listOf(
                        CheckBoxItem(title = "自动使用打开操作", summary =
                        "列表指令失败后，自动使用打开操作\n如：[打开QQ扫一扫] 可以直接使用 [QQ扫一扫] 使用\n" +
                                "或者点击屏幕文字\n" + "*需要无障碍支持",
                                keyId = R.string.key_use_smartopen_if_parse_failed),
                        CheckBoxItem(title = "云解析", summary = "本地解析失败时，使用云解析(暂未开放)",
                                keyId = R.string.key_cloud_service_parse) { _, _ ->
                            GlobalApp.toastInfo("暂未开放")
                            return@CheckBoxItem false//todo true
                        }
//                        ,
//                        CheckBoxItem(R.string.text_only_cloud_parse, summary = "仅高级用户可用",
//                                keyId = R.string.key_only_cloud_service_parse)
                ))
        ).also {
            if (BuildConfig.DEBUG) {
                it.add(SettingGroupItem(R.color.google_red, "调试", childItems = listOf(
                        InputItem(title = "切换服务器", defaultValue = { ApiUrls.anotherIp },
                                summary = ApiUrls.SERVER_IP) { item, s ->
                            ApiUrls.SERVER_IP = s
                            Vog.d("切换服务器: ${ApiUrls.SERVER_IP}")
                            return@InputItem true
                        },
                        IntentItem(title = "触发崩溃") {
                            "a".toInt()
                        },
                        IntentItem(title = "切换引导debug") {
                            Tutorials.debug = !Tutorials.debug
                            GlobalApp.toastInfo("${Tutorials.debug}")
                        },
                        IntentItem(title = "触发低电量") {
                            PowerEventReceiver.onLowBattery()
                        },
                        SwitchItem(title = "无线调试", defaultValue = isWirelessDebugEnable()) { _, i ->
                            wirelessDebug(i)
                            return@SwitchItem false
                        },
                        IntentItem(title = "语音搜索测试") {
                            startActivity(Intent(RecognizerIntent.ACTION_WEB_SEARCH))
                        },
                        IntentItem(title = "语音输入测试") {
                            startActivityForResult(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 200)
                        },
                        IntentItem(title = "屏幕助手") {
                            startActivity(Intent("$packageName.SCREEN_ASSIST"))
                        },
                        IntentItem(title = "扫描屏幕二维码") {
                            startActivity(Intent(this, QrCodeActivity::class.java))
                        },
                        IntentItem(title = "分享屏幕") {
                            startActivity(Intent(this, ScreenShareActivity::class.java))
                        },
                        IntentItem(title = "屏幕识别") {
                            startActivity(Intent(this, SpotScreenActivity::class.java))
                        },
                        IntentItem(title = "文字识别") {
                            startActivity(Intent(this, ScreenOcrActivity::class.java))
                        },
                        IntentItem(title = "若琪测试") {
                            MainService.homeControlSystem?.test()
                        }
                )
                ))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {//选择文件回调
            when (requestCode) {
                1 -> {
                    val uri = data?.data
                    try {
                        if (uri != null) {
                            val path: String = UriUtils.getPathFromUri(this, uri)!!
                            BackupHelper.restoreFromFile(this, File(path))
                        } else {
                            GlobalApp.toastError(getString(R.string.text_open_failed))
                        }
                    } catch (e: Exception) {
                        GlobalApp.toastError("获取失败：" + e.message)
                    }
                }
                200 -> {//语音
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        try {
                            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                            GlobalApp.toastInfo(result[0])
                        } catch (e: Exception) {
                            GlobalApp.toastError(e.message ?: "e")
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getLastInfo(back: (LastDateInfo?) -> Unit) {
        WrapperNetHelper.postJson<LastDateInfo>(ApiUrls.GET_LAST_DATA_DATE) {
            success { _, b ->
                if (b.isOk()) {
                    back.invoke(b.data)
                } else {
                    back.invoke(null)
                }
            }
            fail { _, e ->
                GlobalLog.err(e)
                back.invoke(null)
            }
        }
    }

    private fun showLastDataDate() {
        val p = ProgressDialog(this)
        getLastInfo {
            p.dismiss()
            if (it != null) {
                statistic(it)
            } else {
                GlobalApp.toastError("获取失败")
            }
        }
    }

    private fun statistic(lastInfo: LastDateInfo) {
        val textV = TextView(this)
        textV.setPadding(60, 0, 60, 0)
        val list = mutableListOf<Int>()
        MaterialDialog(this).title(text = "数据更新")
                .customView(view = textV, scrollable = true).show {
                    arrayOf(arrayOf("全局指令", lastInfo.instGlobal, R.string.key_last_sync_global_date)//1
                            , arrayOf("应用内指令", lastInfo.instInApp, R.string.key_last_sync_in_app_date)//2
                            , arrayOf("标记通讯录", lastInfo.markedContact, R.string.key_last_sync_marked_contact_date)//3
                            , arrayOf("标记应用", lastInfo.markedApp, R.string.key_last_sync_marked_app_date)//4
                            , arrayOf("标记打开", lastInfo.markedOpen, R.string.key_last_sync_marked_open_date)//5
                            , arrayOf("标记广告", lastInfo.markedAd, R.string.key_last_sync_marked_ad_date)//6
                    ).withIndex().forEach { kv ->
                        val it = kv.value
                        build(textV, it[0] as String, it[1] as Long?, it[2] as Int).also {
                            if (it) list.add(kv.index)
                        }
                    }
                    positiveButton(text = "一键同步") {
                        DataUpdator.oneKeyUpdate(this@AdvancedSettingActivity, list, null, "")
                    }
                }
    }

    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    val sp: SpHelper by lazy { SpHelper(this) }
    private fun build(view: TextView, pre: String, last: Long?, keyKd: Int): Boolean {
        val lastDate = last ?: -1L
        val lastUpdate = sp.getLong(keyKd)
        val isOutDate = lastDate > lastUpdate

        view.apply {
            append(ColourTextClickableSpan(this@AdvancedSettingActivity, "$pre  ", R.color.primary_text, listener = null).spanStr)
            append(if (isOutDate)
                ColourTextClickableSpan(this@AdvancedSettingActivity, "有更新", R.color.green_700, listener = null).spanStr
            else ColourTextClickableSpan(this@AdvancedSettingActivity, "无更新", R.color.primary_text, listener = null).spanStr)
            append("\n")
            append("上次同步时间: " + (if (lastUpdate > 0) format.format(lastUpdate) else "无") + "\n")
            append("最新服务数据: " + (if (lastDate > 0) format.format(lastDate) else "无") + "\n\n")
        }
        return isOutDate
    }

    private val ipText: String
        get() {
            return "本机IP:" + SystemBridge.getLocalIpAddress() +
                    "\n更多资料请查阅手册"
        }
}
