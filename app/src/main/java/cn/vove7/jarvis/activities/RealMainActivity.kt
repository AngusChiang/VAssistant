package cn.vove7.jarvis.activities

import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import cn.vove7.common.model.UserInfo
import cn.vove7.jarvis.R
import cn.vove7.jarvis.fragments.HomeFragment
import cn.vove7.jarvis.fragments.MineFragment
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.AppConfig.checkAppUpdate
import cn.vove7.jarvis.tools.DataUpdator
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.view.dialog.UpdateLogDialog
import cn.vove7.jarvis.view.tools.FragmentSwitcher
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_real_main.*
import kotlinx.android.synthetic.main.fragment_mine.*


class RealMainActivity : AppCompatActivity() {

    private val fSwitcher = FragmentSwitcher(this, R.id.fragment)
    private val homeF = HomeFragment.newInstance()
    //    val storeF = StoreFragment.newInstance()
    private val mineF = MineFragment.newInstance()
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = resources.getColor(R.color.app_background)
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.nav_me
        fSwitcher.switchFragment(mineF)

        requestPermission()
    }

    var firstIn = true
    var lastCheck = 0L
    override fun onResume() {
        super.onResume()
        if (!firstIn) {
            //检查数据更新
            val now = System.currentTimeMillis()
            if (now - lastCheck > 120000) {
                checkDataUpdate()
                checkAppUpdate(this, false)
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
                android.Manifest.permission.READ_PHONE_STATE
        )
        var showUpdate = true
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

    override fun onDestroy() {
        super.onDestroy()
        System.gc()
    }
    private fun checkDataUpdate() {
        if (AppConfig.autoUpdateData) {
            DataUpdator.checkUpdate(this) {
                if (!UserInfo.isLogin())
                    text_login?.post {
                        Tutorials.showForView(this, Tutorials.T_LOGIN, text_login,
                                "新用户注册", getString(R.string.desc_new_account))
                    }
            }
        }
    }

    private fun showDataUpdateLog() {
        lastCheck = System.currentTimeMillis()
        if (AppConfig.FIRST_LAUNCH_NEW_VERSION && showUpdate) {
            UpdateLogDialog(this) {
                Vog.d("检查数据更新")
                checkDataUpdate()
            }
            showUpdate = false
        } else {
            checkDataUpdate()
            checkAppUpdate(this, false)
        }
    }

}
