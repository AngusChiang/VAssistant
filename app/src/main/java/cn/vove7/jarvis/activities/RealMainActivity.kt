package cn.vove7.jarvis.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.view.View
import android.view.Window
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.AppConfig.checkAppUpdate
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.StubbornFlag
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.fragments.HomeFragment
import cn.vove7.jarvis.fragments.MineFragment
import cn.vove7.jarvis.receivers.UtilEventReceiver
import cn.vove7.jarvis.tools.AppNotification
import cn.vove7.jarvis.tools.DataUpdator
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.view.dialog.UpdateLogDialog
import cn.vove7.jarvis.view.tools.FragmentSwitcher
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_real_main.*
import kotlinx.android.synthetic.main.fragment_mine.*


/**
 *
 * 初始化流程
 *
 */
class RealMainActivity : BaseActivity() {

    private val fSwitcher = FragmentSwitcher(this, R.id.fragment)
    private val homeF by lazy { HomeFragment.newInstance() }
    //    val storeF = StoreFragment.newInstance()
    private val mineF by lazy { MineFragment.newInstance() }
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        return@OnNavigationItemSelectedListener when (item.itemId) {
            R.id.nav_home -> fSwitcher.switchFragment(homeF)
//            R.id.nav_store -> fSwitcher.switchFragment(storeF)
            R.id.nav_me -> fSwitcher.switchFragment(mineF)
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_real_main)

        initView()
        requestPermission()
    }

    private fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = resources.getColor(R.color.app_background)
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = if (AppConfig.FIRST_LAUNCH_NEW_VERSION && inFlag) R.id.nav_home else R.id.nav_me
    }


    override fun onResume() {
        super.onResume()
        val now = System.currentTimeMillis()

        if (now - lastCheckApp > 10 * 60000) {
            checkAppUpdate(this, false, onUpdate)
            lastCheckApp = now
        }

        if (!firstIn) {
            //检查数据更新
            if (now - lastCheck > 120000) {
                checkDataUpdate()
                if (AppConfig.autoCheckPluginUpdate)// 插件更新
                    DataUpdator.checkPluginUpdate()
                lastCheck = now
            }
        } else
            firstIn = false
    }

    companion object {
        val ps = arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                android.Manifest.permission.READ_PHONE_STATE
        )

        var showUpdate = true//显示更新日志

        private var firstIn = true

        private var lastCheck = 0L

        var lastCheckApp = 0L

        var inFlag by StubbornFlag(initValue = true, afterValue = false)
    }

    private fun requestPermission() {
        if (!PermissionUtils.isAllGranted(this, ps)) {
            MaterialDialog(this).title(text = "请先授予必要的权限")
                    .message(text = "1. 麦克风\n2. 存储权限\n需要其他权限可到权限管理中开启").positiveButton {
                        PermissionUtils.autoRequestPermission(this, ps)
                    }.show()
        } else {
            showDataUpdateLog()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        showDataUpdateLog()
    }

    private fun checkDataUpdate() {
        DataUpdator.checkUpdate(this) {
            if (!UserInfo.isLogin())
                text_login?.post {
                    Tutorials.showForView(this, Tutorials.T_LOGIN, text_login,
                            "新用户注册", getString(R.string.desc_new_account))
                }
        }
    }

    private fun showDataUpdateLog() {
        lastCheck = System.currentTimeMillis()
        if (AppConfig.FIRST_LAUNCH_NEW_VERSION && showUpdate && !BuildConfig.DEBUG) {
            UpdateLogDialog(this) {
                Vog.d("检查数据更新")
                checkDataUpdate()
            }
            showUpdate = false
        } else {
            runOnNewHandlerThread(delay = 2000) {
                checkDataUpdate()
            }
        }
    }

    private val onUpdate: (Pair<String, String>?) -> Unit
        get() = a@{ hasUpdate ->
            hasUpdate ?: return@a
            AppNotification.broadcastNotification(
                    123, "发现新版本 ${hasUpdate.first}",
                    "查看更新日志",
                    (Intent(UtilEventReceiver.APP_HAS_UPDATE).apply {
                        putExtra("version", hasUpdate.first)
                        putExtra("log", hasUpdate.second)
                    })
            )
        }
}
