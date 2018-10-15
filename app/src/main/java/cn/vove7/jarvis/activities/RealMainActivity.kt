package cn.vove7.jarvis.activities

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.netacc.model.LastDateInfo
import cn.vove7.jarvis.R
import cn.vove7.jarvis.fragments.HomeFragment
import cn.vove7.jarvis.fragments.MineFragment
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.jarvis.utils.DataUpdator
import cn.vove7.jarvis.view.utils.FragmentSwitcher
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
import cn.vove7.vtp.sharedpreference.SpHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import kotlinx.android.synthetic.main.activity_real_main.*


class RealMainActivity : AppCompatActivity() {

    val fSwitcher = FragmentSwitcher(this, R.id.fragment)
    val homeF = HomeFragment.newInstance()
    //    val storeF = StoreFragment.newInstance()
    val mineF = MineFragment.newInstance()
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

    companion object {
        val ps = arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.RECORD_AUDIO
        )
    }

    private fun requestPermission() {
        if (!PermissionUtils.isAllGranted(this, ps)) {
            MaterialDialog(this).title(text = "请先授予必要的权限")
                    .message(text = "1. 麦克风\n2. 存储权限\n需要其他权限可到权限管理开启").positiveButton {
                        PermissionUtils.autoRequestPermission(this, ps)
                    }.show()
        } else {
            val sp = SpHelper(this)
            if (sp.getBoolean("first_in", true)) {
                userGuide()
                sp.set("first_in", false)
            } else {
                checkDataUpdate()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        userGuide()
    }

    private fun checkDataUpdate() {
        if(AppConfig.autoUpdateData) {
            DataUpdator.checkUpdate(this)
        }
    }


    private fun userGuide() {
        MaterialDialog(this).title(text = "引导")
                .message(text = "1. 首次使用，请至帮助中仔细阅读[使用手册]\n" +
                        "2. 首次使用，可在注册登陆后至指令管理，标记管理中同步最新数据")
                .positiveButton()
                .negativeButton()
                .onDismiss {
                    checkDataUpdate()
                }
                .show()
    }

}
