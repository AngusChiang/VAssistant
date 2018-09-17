package cn.vove7.jarvis.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import cn.vove7.androlua.LuaEditorActivity
import cn.vove7.jarvis.R
import cn.vove7.jarvis.utils.debugserver.RemoteDebugServer
import cn.vove7.jarvis.view.IntentItem
import cn.vove7.jarvis.view.SwitchItemWithoutSp
import cn.vove7.jarvis.view.custom.SettingGroupItem
import cn.vove7.jarvis.view.custom.SettingsExpandableAdapter
import kotlinx.android.synthetic.main.activity_expandable_settings.*

/**
 * # AdvancedSettingActivity
 *
 * @author 17719247306
 * 2018/9/10
 */
class AdvancedSettingActivity : AppCompatActivity() {

    lateinit var groupItems: List<SettingGroupItem>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        initData()
        setContentView(R.layout.activity_expandable_settings)
        val adapter = SettingsExpandableAdapter(this, groupItems, expand_list)
        expand_list.setAdapter(adapter)

        expand_list.post {
            expand_list.expandGroup(0)
            expand_list.expandGroup(1)
        }
    }

    fun initData() {
        groupItems = listOf(
                SettingGroupItem(R.color.google_blue, "管理", childItems = listOf(
                        IntentItem(R.string.instru_management, onClick = { _, _ ->
                            startActivity(Intent(this, InstManagerActivity::class.java))
                        }) ,
                        IntentItem(R.string.text_mark_management, onClick = { _, _ ->
                            startActivity(Intent(this, MarkedManagerActivity::class.java))
                        })
                )),
                SettingGroupItem(R.color.google_green, "...", childItems = listOf(
                        SwitchItemWithoutSp(R.string.text_remote_debug, null, defaultValue = {
                            !RemoteDebugServer.stopped
                        }, callback = { holder, it ->
                            if (it as Boolean) RemoteDebugServer.start()
                            else RemoteDebugServer.stop()
                        }),
                        IntentItem(R.string.text_test_code, null, onClick = { _, _ ->
                            startActivity(Intent(this, LuaEditorActivity::class.java))
                        })
                ))
        )
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}
