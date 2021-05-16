package cn.vove7.jarvis.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.adapters.IconTitleEntity
import cn.vove7.jarvis.adapters.IconTitleListAdapter
import cn.vove7.jarvis.databinding.ActivityAbcHeaderBinding
import cn.vove7.jarvis.databinding.HeaderAboutBinding
import cn.vove7.jarvis.tools.openQQChat
import cn.vove7.jarvis.view.dialog.AppUpdateDialog
import cn.vove7.jarvis.view.dialog.ProgressDialog
import cn.vove7.jarvis.view.dialog.UpdateLogDialog
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.sharedpreference.SpHelper
import cn.vove7.vtp.system.SystemHelper
import cn.vove7.vtp.system.SystemHelper.APP_STORE_COLL_APK


/**
 * # AboutActivity
 *
 * @author Administrator
 * 9/23/2018
 */
class AboutActivity : BaseActivity<ActivityAbcHeaderBinding>() {
    override val darkTheme: Int
        get() = R.style.DarkTheme

    private var clickTime = 0L
    private var clickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val headerVB = HeaderAboutBinding.inflate(layoutInflater)
        viewBinding.headerContent.addView(headerVB.root)

        viewBinding.root.setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - clickTime > 1000) {//开始点击
                clickTime = now
                clickCount = 0
            }
            if (++clickCount > 4) {
                clickTime = 0
                enterDevMode()
            }
        }
        headerVB.verNameView.text = AppConfig.versionName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewBinding.listView.adapter = IconTitleListAdapter(this, getData())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun enterDevMode() {//彩蛋
        if (AppConfig.devMode) {
            SpHelper(this).set(R.string.key_dev_mode, false)
            GlobalApp.toastSuccess("关闭开发模式", Toast.LENGTH_LONG)
        } else {
            SpHelper(this).set(R.string.key_dev_mode, true)
            GlobalApp.toastSuccess("进入开发模式", Toast.LENGTH_LONG)
        }
    }

    class VH(v: View) : BaseListAdapter.ViewHolder(v) {
        val iconView = v.findViewById<ImageView>(R.id.icon)!!
        val titleView = v.findViewById<TextView>(R.id.title)!!
        val subTitleView = v.findViewById<TextView>(R.id.sub_title)!!
    }

    private fun getData(): List<IconTitleEntity> = listOf(
            IconTitleEntity(R.drawable.ic_favorite_border_24dp, R.string.text_favor_it) {
                SystemHelper.openApplicationMarket(this, this.packageName, APP_STORE_COLL_APK)
            },
            IconTitleEntity(R.drawable.ic_update_24dp, R.string.text_check_for_updates) {

                val p = ProgressDialog(this) {
                    GlobalApp.toastError("检查失败")
                }
                AppConfig.checkAppUpdate(this, true) {
                    p.dismiss()
                    if (it == null) {
                        GlobalApp.toastSuccess("未发现新版本")
                    } else {
                        if (!isFinishing) {
                            AppUpdateDialog(this, it.first, it.second)
                        }
                    }
                }
            },
            IconTitleEntity(null, R.string.text_vosp, R.string.text_vosp_summary) {
                SystemBridge.openUrl("https://github.com/Vove7/VOSP")
            },
            IconTitleEntity(R.drawable.ic_github, R.string.text_open_source_libraries) {
                startActivity(Intent(this, OSLActivity::class.java))

            },
            IconTitleEntity(R.drawable.ic_qq, R.string.text_contact_me, R.string.text_contact_qq) {
                openQQChat("529545532")
            },
            IconTitleEntity(R.drawable.ic_update_24dp, R.string.text_update_log) {
                UpdateLogDialog(this)
            }
    )
}

