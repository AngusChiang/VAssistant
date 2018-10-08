package cn.vove7.jarvis.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import cn.vove7.androlua.LuaEditorActivity
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.model.LastDateInfo
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.adapters.SettingsExpandableAdapter
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.jarvis.utils.NetHelper
import cn.vove7.jarvis.utils.debugserver.RemoteDebugServer
import cn.vove7.jarvis.view.CheckBoxItem
import cn.vove7.jarvis.view.IntentItem
import cn.vove7.jarvis.view.SwitchItem
import cn.vove7.jarvis.view.custom.SettingGroupItem
import cn.vove7.jarvis.view.dialog.LoginDialog
import cn.vove7.jarvis.view.dialog.ProgressDialog
import cn.vove7.jarvis.view.dialog.UserInfoDialog
import cn.vove7.jarvis.view.utils.SettingItemHelper
import cn.vove7.rhino.RhinoActivity
import cn.vove7.vtp.sharedpreference.SpHelper
import cn.vove7.vtp.view.span.ColourTextClickableSpan
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import kotlinx.android.synthetic.main.activity_expandable_settings.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * # AdvancedSettingActivity
 *
 * @author 17719247306
 * 2018/9/10
 */
class AdvancedSettingActivity : ReturnableActivity() {

    val toast: ColorfulToast by lazy { ColorfulToast(this).blue() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expandable_settings)
        val adapter = SettingsExpandableAdapter(this, groupItems, expand_list)
        expand_list.setAdapter(adapter)

        expand_list.post {
            expand_list.expandGroup(0)
            expand_list.expandGroup(1)
        }
        btn_unlock.setOnClickListener {
            if (UserInfo.isLogin()) {
                UserInfoDialog(this) {}
            } else {
                toast.showLong(R.string.text_please_login_first)
                LoginDialog(this) {
                    if (UserInfo.isVip()) {
                        unlock_advan_fun.visibility = View.GONE
                    } else
                        UserInfoDialog(this) {}
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
                        }),
                        IntentItem(R.string.text_mark_management, onClick = { _, _ ->
                            startActivity(Intent(this, MarkedManagerActivity::class.java))
                        }),
                        IntentItem(R.string.text_check_last_data, onClick = { _, _ ->
                            showLastDataDate()
                        })
                )),
                SettingGroupItem(R.color.google_green, "脚本", childItems = listOf(
                        SwitchItem(R.string.text_remote_debug, summary = if (RemoteDebugServer.stopped) "使用Pc调试，请查阅使用手册"
                        else ipText, defaultValue = {
                            !RemoteDebugServer.stopped
                        }, callback = { holder, it ->
                            if (!AppConfig.checkUser()) {
                                (holder as SettingItemHelper.SwitchItemHolder).compoundWight.isChecked = false
                                return@SwitchItem
                            }

                            if (it as Boolean) {
                                RemoteDebugServer.start()
                                holder.summaryView.text = ipText
                            } else RemoteDebugServer.stop()
                        }),
                        IntentItem(R.string.text_test_code_lua, onClick = { _, _ ->
                            if (AppConfig.checkUser()) {
                                startActivity(Intent(this, LuaEditorActivity::class.java))
                            }
                        }),
                        IntentItem(R.string.text_code_test_js, null, onClick = { _, _ ->
                            if (AppConfig.checkUser()) {
                                startActivity(Intent(this, RhinoActivity::class.java))
                            }
                        })
                )),
                SettingGroupItem(R.color.google_red, "备份", childItems = listOf(
                        IntentItem(title = "备份到本地") { _, _ ->
                            //todo
                            toast.showShort(R.string.text_coming_soon)
                        },
                        IntentItem(title = "备份到云端") { _, _ ->
                            //todo
                            toast.showShort(R.string.text_coming_soon)
                        }
                )),
                SettingGroupItem(R.color.teal_A700, "命令解析", childItems = listOf(
                        CheckBoxItem(title = "自动使用打开操作", summary =
                        "列表指令失败后，自动使用打开操作\n如：[打开QQ扫一扫] 可以直接使用 [QQ扫一扫] 使用",
                                keyId = R.string.key_use_smartopen_if_parse_failed),
                        CheckBoxItem(title = "云解析", summary = "本地解析失败时，使用云解析",
                                keyId = R.string.key_cloud_service_parse),
                        CheckBoxItem(R.string.text_only_cloud_parse, summary = "仅高级用户可用",
                                keyId = R.string.key_only_cloud_service_parse)
                ))
        ).also {
            if (BuildConfig.DEBUG) {
                it.add(SettingGroupItem(R.color.google_red, "..", childItems = listOf(
                        SwitchItem(title = "切换服务器", defaultValue = { false },
                                summary = ApiUrls.SERVER_IP) { h, b ->
                            ApiUrls.switch()
                            h.summaryView.text = ApiUrls.SERVER_IP
                        }
                )))
            }
        }
    }

    private fun showLastDataDate() {
        val p = ProgressDialog(this)

        NetHelper.postJson<LastDateInfo>(ApiUrls.GET_LAST_DATA_DATE, type = NetHelper.LastDateInfoType) { _, b ->
            p.dismiss()
            if (b?.isOk() == true && b.data != null) {
                statistic(b.data!!)
            } else {
                toast.showShort("获取失败")
            }
        }

    }

    private fun statistic(lastInfo: LastDateInfo) {
        val textV = TextView(this)
        textV.setPadding(60, 0, 60, 0)
        MaterialDialog(this).title(text = "数据更新")
                .customView(view = textV, scrollable = true).show {
                    arrayOf(
                            arrayOf("全局指令", lastInfo.instGlobal, R.string.key_last_sync_global_date)
                            , arrayOf("应用内指令", lastInfo.instInApp, R.string.key_last_sync_in_app_date)
                            , arrayOf("标记通讯录", lastInfo.markedContact, R.string.key_last_sync_marked_contact_date)
                            , arrayOf("标记应用", lastInfo.markedApp, R.string.key_last_sync_marked_app_date)
                            , arrayOf("标记打开", lastInfo.markedOpen, R.string.key_last_sync_marked_open_date)
                            , arrayOf("标记广告", lastInfo.markedAd, R.string.key_last_sync_marked_ad_date)
                    ).forEach {
                        build(textV, it[0] as String, it[1] as Long?, it[2] as Int)
                    }
                }
    }

    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    val sp: SpHelper by lazy { SpHelper(this) }
    private fun build(view: TextView, pre: String, last: Long?, keyKd: Int) {
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
    }

    val ipText: String
        get() {
            return "本机IP:" + SystemBridge.getIpAddress() +
                    "\n更多资料请查阅手册"
        }
}
