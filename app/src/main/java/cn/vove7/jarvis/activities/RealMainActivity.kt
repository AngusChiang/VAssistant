package cn.vove7.jarvis.activities

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
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
        setContentView(R.layout.activity_real_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.nav_me
        fSwitcher.switchFragment(mineF)

        requestPermission()
    }

    var firstIn = true
    override fun onResume() {
        super.onResume()
        if (!firstIn) {
            checkDataUpdate()
            checkAppUpdate(this, false)
        } else
            firstIn = false
        if (AppConfig.autoCheckPluginUpdate)// 插件更新
            DataUpdator.checkPluginUpdate()
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
        if (AppConfig.FIRST_LAUNCH_NEW_VERSION && showUpdate) {
            UpdateLogDialog(this) {
                checkDataUpdate()
            }
            showUpdate = false
        } else {
            checkDataUpdate()
            checkAppUpdate(this, false)
        }
    }

}
