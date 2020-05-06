package cn.vove7.jarvis.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import cn.vove7.common.app.AppConfig
import cn.vove7.common.model.UserInfo
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.tools.debugserver.RemoteDebugServer
import cn.vove7.jarvis.view.dialog.UpdateLogDialog
import cn.vove7.jarvis.work.DataSyncWork
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.fragment_mine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay


/**
 *
 * 初始化流程
 *
 */
class MainActivity : BaseActivity() {

    override val layoutRes: Int
        get() = R.layout.activity_real_main

    override fun onBackPressed() {
        finishAndRemoveTask()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        requestPermission()
        checkDebug()
        syncDataOnFirstIn()
    }

    private fun syncDataOnFirstIn() {
        if (AppConfig.FIRST_IN) {
            DataSyncWork.startOnce()
        }
    }

    private fun checkDebug() {
        if (BuildConfig.DEBUG && intent.hasExtra("debug")) {
            RemoteDebugServer.start()
        }
    }

    override fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isDarkTheme) {
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.statusBarColor = resources!!.getColor(R.color.app_background, theme)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!firstIn) {
            //检查数据更新
            showTutorials()
        } else {
            firstIn = false
        }
    }

    private fun showTipRecent() {
        MaterialDialog(this).show {
            title(text = "如何从最近任务隐藏？")
            cancelable(false)
            message(text = "在主页通过按返回键（侧边返回）到桌面即可隐藏卡片，为加强后台能力，可在本页面进入最近任务将本App上锁。")
            positiveButton(text = "我知道了，不再提示")
        }
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

    }

    private fun requestPermission() {
        if (!PermissionUtils.isAllGranted(this, ps)) {
            MaterialDialog(this)
                    .title(text = "请先授予必要的权限")
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

    private fun showTutorials() {
        if (!UserInfo.isLogin()) {
            text_login?.post {
                Tutorials.showForView(this, Tutorials.T_LOGIN, text_login,
                        "新用户注册", getString(R.string.desc_new_account))
            }
        }
    }

    private fun showDataUpdateLog() {
        if (AppConfig.FIRST_LAUNCH_NEW_VERSION && showUpdate && !BuildConfig.DEBUG) {
            UpdateLogDialog(this) {
                Vog.d("检查数据更新")
                showTutorials()
            }
            showUpdate = false
        } else {
            launch(Dispatchers.Main) {
                delay(2000)
                showTutorials()
            }
        }
    }

}
