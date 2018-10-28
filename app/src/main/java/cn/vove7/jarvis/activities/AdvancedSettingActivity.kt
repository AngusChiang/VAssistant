package cn.vove7.jarvis.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.netacc.model.LastDateInfo
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.adapters.SettingsExpandableAdapter
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.DataUpdator
import cn.vove7.jarvis.tools.UriUtils
import cn.vove7.jarvis.tools.backup.BackupHelper
import cn.vove7.jarvis.tools.debugserver.RemoteDebugServer
import cn.vove7.jarvis.view.CheckBoxItem
import cn.vove7.jarvis.view.IntentItem
import cn.vove7.jarvis.view.SwitchItem
import cn.vove7.jarvis.view.custom.SettingGroupItem
import cn.vove7.jarvis.view.dialog.LoginDialog
import cn.vove7.jarvis.view.dialog.ProgressDialog
import cn.vove7.jarvis.view.dialog.UserInfoDialog
import cn.vove7.jarvis.view.utils.SettingItemHelper
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expandable_settings)
        val expandableListView = expand_list
        val adapter = SettingsExpandableAdapter(this, groupItems, expandableListView)

        expandableListView?.setAdapter(adapter)

        expandableListView?.post {
            expandableListView.apply {
                expandGroup(0)
                expandGroup(1)
            }
        }

        btn_unlock.setOnClickListener {
            if (UserInfo.isLogin()) {
                UserInfoDialog(this) {}
            } else {
                toast.showLong(R.string.text_please_login_first)
                LoginDialog(this) {
                    if (UserInfo.isVip()) {
                        unlock_advan_fun.visibility = View.GONE
                    } else UserInfoDialog(this) {}
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppConfig.checkDate()
        unlock_advan_fun.visibility = if (UserInfo.isVip()) {
            View.GONE
        } else View.VISIBLE
    }

    private val groupItems: List<SettingGroupItem> by lazy {
        mutableListOf(
                SettingGroupItem(R.color.google_blue, "管理", childItems = listOf(
                        IntentItem(R.string.instru_management, onClick = { _, _ ->
                            startActivity(Intent(this, InstManagerActivity::class.java))
                            return@IntentItem true
                        }),
                        IntentItem(R.string.text_mark_management, onClick = { _, _ ->
                            startActivity(Intent(this, MarkedManagerActivity::class.java))
                            return@IntentItem true
                        }),
                        IntentItem(R.string.text_check_last_data, onClick = { _, _ ->
                            showLastDataDate()
                            return@IntentItem true
                        }),
                        CheckBoxItem(title = "自动更新", summary = "在进入App后自动检查并更新最新数据",
                                keyId = R.string.key_auto_update_data, defaultValue = { true })
                )),
                SettingGroupItem(R.color.google_green, "脚本", childItems = listOf(
                        SwitchItem(R.string.text_remote_debug, summary = if (RemoteDebugServer.stopped) "使用Pc调试，请查阅使用手册"
                        else ipText, defaultValue = {
                            !RemoteDebugServer.stopped
                        }, callback = { holder, it ->
                            if (!AppConfig.checkUser()) {
                                (holder as SettingItemHelper.SwitchItemHolder).compoundWight.isChecked = false
                                return@SwitchItem true
                            }

                            if (it as Boolean) {
                                RemoteDebugServer.start()
                                holder.summaryView.text = ipText
                            } else RemoteDebugServer.stop()
                            return@SwitchItem true
                        }),
                        IntentItem(R.string.text_test_code_lua, onClick = { _, _ ->
                            if (AppConfig.checkUser()) {
                                startActivity(Intent(this, LuaEditorActivity::class.java).also { it.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT) })
                            }
                            return@IntentItem true
                        }),
                        IntentItem(R.string.text_code_test_js, null, onClick = { _, _ ->
                            if (AppConfig.checkUser()) {
                                startActivity(Intent(this, JsEditorActivity::class.java).also { it.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT) })
                            }
                            return@IntentItem true
                        })
                )),
                SettingGroupItem(R.color.google_red, "备份", childItems = listOf(
                        IntentItem(title = "备份") { _, _ ->
                            if (UserInfo.isLogin()) {
                                BackupHelper.showBackupDialog(this)
                            } else {
                                toast.showShort("请登录后操作")
                            }
                            return@IntentItem true
                        },
                        IntentItem(title = "从本地恢复") { _, _ ->
                            if (UserInfo.isLogin()) {
                                BackupHelper.showBackupFileList(this)
                            } else {
                                toast.showShort("请登录后操作")
                            }
                            return@IntentItem true
                        },
                        IntentItem(title = "查看云端备份") { _, _ ->
                            //todo
                            toast.showShort(R.string.text_coming_soon)
                            return@IntentItem true
                        }
                )),
                SettingGroupItem(R.color.teal_A700, "命令解析", childItems = listOf(
                        CheckBoxItem(title = "自动使用打开操作", summary =
                        "列表指令失败后，自动使用打开操作\n如：[打开QQ扫一扫] 可以直接使用 [QQ扫一扫] 使用\n" +
                                "或者点击屏幕文字\n" +
                                "需要无障碍支持",
                                keyId = R.string.key_use_smartopen_if_parse_failed),
                        CheckBoxItem(title = "云解析", summary = "本地解析失败时，使用云解析",
                                keyId = R.string.key_cloud_service_parse)
//                        ,
//                        CheckBoxItem(R.string.text_only_cloud_parse, summary = "仅高级用户可用",
//                                keyId = R.string.key_only_cloud_service_parse)
                ))
        ).also {
            if (BuildConfig.DEBUG) {
                it.add(SettingGroupItem(R.color.google_red, "..", childItems = listOf(
                        SwitchItem(title = "切换服务器", defaultValue = { false },
                                summary = ApiUrls.SERVER_IP) { h, b ->
                            ApiUrls.switch()
                            h.summaryView.text = ApiUrls.SERVER_IP
                            return@SwitchItem true
                        }
                )))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {//选择文件回调
            when (requestCode) {
                1 -> {
                    val uri = data?.data
                    if (uri != null) {
                        val path = UriUtils.getPathFromUri(this, uri)!!
                        BackupHelper.restoreFromFile(this, File(path))
                    } else {
                        toast.showShort(getString(R.string.text_open_failed))
                    }
                }

            }
        }
    }

    private fun showLastDataDate() {
        val p = ProgressDialog(this)
        NetHelper.getLastInfo {
            p.dismiss()
            if (it != null) {
                statistic(it)
            } else {
                toast.showShort("获取失败")
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
                        DataUpdator.onKeyUpdate(this@AdvancedSettingActivity, list)
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
