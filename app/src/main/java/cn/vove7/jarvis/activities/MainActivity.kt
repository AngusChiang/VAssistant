package cn.vove7.jarvis.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import cn.vove7.android.common.logd
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.model.UserInfo
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.databinding.ActivityRealMainBinding
import cn.vove7.jarvis.services.ForegroundService
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.tools.debugserver.RemoteDebugServer
import cn.vove7.jarvis.view.dialog.UpdateLogDialog
import cn.vove7.jarvis.work.DataSyncWork
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
import cn.vove7.vtp.weaklazy.weakLazy
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay


/**
 *
 * 初始化流程
 *
 */
class MainActivity : BaseActivity<ActivityRealMainBinding>() {

    override fun onBackPressed() {
        finishAndRemoveTask()
    }

    private val homeReceiver by lazy {
        GoHomeReceiver {
            if (Tutorials.tipsHideRecent) {
                tipsRecentDialog.show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        requestPermission()
        checkDebug()
        syncDataOnFirstIn()
        homeReceiver.start(this)
        if (GlobalApp.ForeService !is ForegroundService) {
            ForegroundService.start(this)
        }
    }

    override fun onDestroy() {
        homeReceiver.stop(this)
        super.onDestroy()
    }

    private fun syncDataOnFirstIn() {
        if (AppConfig.FIRST_IN && firstIn) {
            DataSyncWork.startOnce()
        }
    }

    private fun checkDebug() {
        if (BuildConfig.DEBUG && intent.hasExtra("debug")) {
            RemoteDebugServer.start()
        }
    }

    override fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isDarkTheme) {
            wic?.isAppearanceLightNavigationBars = true
            wic?.isAppearanceLightStatusBars = true
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

    private val tipsRecentDialog by weakLazy {
        MaterialDialog(this).apply {
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
            findViewById<View>(R.id.text_login)?.post {
                Tutorials.showForView(this, Tutorials.T_LOGIN, findViewById(R.id.text_login),
                    "新用户注册", getString(R.string.desc_new_account))
            }
        }
    }

    private fun showDataUpdateLog() {
        if (AppConfig.FIRST_LAUNCH_NEW_VERSION && showUpdate) {
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

    class GoHomeReceiver(val onGo: (reason: String?) -> Unit) : BroadcastReceiver() {
        fun start(context: Context) {
            val itf = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.registerReceiver(this, itf)
        }

        fun stop(context: Context) {
            context.unregisterReceiver(this)
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra("reason")
                reason.logd()
                onGo(reason)
            }
        }

        private val SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps" //home键旁边的最近程序列表键
        private val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey" //按下home键
        private val SYSTEM_DIALOG_REASON_LOCK = "lock" //锁屏键
        private val SYSTEM_DIALOG_REASON_DREAM = "dream" //锁屏键
        private val SYSTEM_DIALOG_REASON_ASSIST = "assist" //某些三星手机的程序列表键
    }

}
