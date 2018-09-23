package cn.vove7.jarvis.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import cn.vove7.androlua.LuaEditorActivity
import cn.vove7.common.model.UserInfo
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.ReturnableActivity
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.jarvis.utils.debugserver.RemoteDebugServer
import cn.vove7.jarvis.view.IntentItem
import cn.vove7.jarvis.view.SwitchItemWithoutSp
import cn.vove7.jarvis.view.custom.SettingGroupItem
import cn.vove7.jarvis.view.custom.SettingsExpandableAdapter
import cn.vove7.jarvis.view.dialog.LoginDialog
import cn.vove7.jarvis.view.dialog.UserInfoDialog
import cn.vove7.jarvis.view.utils.SettingItemHelper
import cn.vove7.rhino.RhinoActivity
import kotlinx.android.synthetic.main.activity_expandable_settings.*

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

        unlock_advan_fun.visibility = if (UserInfo.isVip()) {
            View.GONE
        } else View.VISIBLE

    }


    private val groupItems: List<SettingGroupItem> by lazy {
        listOf(
                SettingGroupItem(R.color.google_blue, "管理", childItems = listOf(
                        IntentItem(R.string.instru_management, onClick = { _, _ ->
                            startActivity(Intent(this, InstManagerActivity::class.java))
                        }),
                        IntentItem(R.string.text_mark_management, onClick = { _, _ ->
                            startActivity(Intent(this, MarkedManagerActivity::class.java))
                        })
                )),
                SettingGroupItem(R.color.google_green, "脚本", childItems = listOf(
                        SwitchItemWithoutSp(R.string.text_remote_debug, summary = "", defaultValue = {
                            !RemoteDebugServer.stopped
                        }, callback = { holder, it ->
                            if (!AppConfig.checkUser()) {
                                (holder as SettingItemHelper.SwitchItemHolder).compoundWight.toggle()
                                return@SwitchItemWithoutSp
                            }

                            if (it as Boolean) RemoteDebugServer.start()
                            else RemoteDebugServer.stop()
                        }),
                        IntentItem(R.string.text_test_code_lua, null, onClick = { _, _ ->
                            if (AppConfig.checkUser()) {
                                startActivity(Intent(this, LuaEditorActivity::class.java))
                            }
                        }),
                        IntentItem(R.string.text_code_test_js, null, onClick = { _, _ ->
                            if (AppConfig.checkUser()) {
                                startActivity(Intent(this, RhinoActivity::class.java))
                            }
                        })
                ))
        )
    }
}
