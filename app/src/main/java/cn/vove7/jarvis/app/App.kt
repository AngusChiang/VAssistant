package cn.vove7.jarvis.app

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.work.WorkManager
import cn.daqinjia.android.common.ext.delayRun
import cn.daqinjia.android.scaffold.ui.base.ScaffoldActivity
import cn.vove7.bottomdialog.builder.BottomDialogBuilder
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.utils.md5
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.jarvis.R
import cn.vove7.jarvis.plugins.PowerListener
import cn.vove7.jarvis.receivers.AppInstallReceiver
import cn.vove7.jarvis.receivers.PowerEventReceiver
import cn.vove7.jarvis.receivers.ScreenStatusListener
import cn.vove7.jarvis.receivers.UtilEventReceiver
import cn.vove7.jarvis.services.ForegroundService
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.*
import cn.vove7.jarvis.tools.debugserver.ConnectiveService
import cn.vove7.jarvis.tools.timedtask.TimedTaskManager
import cn.vove7.jarvis.work.DataSyncWork
import cn.vove7.quantumclock.QuantumClock
import cn.vove7.quantumclock.synchers.TaoBaoSyncher
import cn.vove7.smartkey.android.AndroidSettings
import cn.vove7.vtp.log.Vog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


@Suppress("MemberVisibilityCanBePrivate")
class App : GlobalApp() {

    override fun onCreate() {
        CrashHandler.install()
        super.onCreate()
        if (!isMainProcess) {
            Vog.d("非主进程")
            return
        }
        AndroidSettings.init(this)

        startMainServices()
        ScaffoldActivity.apply {
            enableThamable = true
            globalDarkTheme = R.style.DarkTheme_NoActionBar
        }

        BottomDialogBuilder.apply {
            enableAutoDarkTheme = true
            darkTheme = R.style.BottomDialog_Dark
        }
        WorkManager.getInstance(this).cancelAllWork()

    }

    private fun startMainServices() {
        MainService.start()
    }

    //供脚本api
    override fun startActivity(intent: Intent?) {
        intent?.fixApplicationNewTask()?.let {
            super.startActivity(it)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        AdvanAppHelper.trimMem()
    }
}

class InitCp : ContentProvider() {

    override fun onCreate(): Boolean {
        delayRun(2000) {

            QuantumClock.apply {
                removeSyncer(TaoBaoSyncher)
                addSyncer(MyTimeSyncher)
                sync().invokeOnCompletion {
                    AppLogic.onLaunch()
                }
            }

            openAccessibilityServiceAuto()
            setAssistantAppAuto()
            if (AppConfig.connectiveService) {
                ConnectiveService.start()
            }

            val wm = WorkManager.getInstance(GlobalApp.APP)
            wm.enqueue(DataSyncWork.getRequest())

            ShortcutUtil.initShortcut()
            AdvanAppHelper.getPkgList()
            startBroadcastReceivers()
            launchExtension()

            val foreService = Intent(context, ForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(foreService)
            } else {
                context?.startService(foreService)
            }
            a().c()
            TimedTaskManager.init()
        }
        return true
    }

    class a {
        fun c() = GlobalScope.launch {
            val pm = GlobalApp.APP.packageManager
            val pkg = GlobalApp.APP.packageName
            val ss = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo.apkContentsSigners
            } else pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES).signatures
            if (ss?.isNotEmpty() == true) {
                val smd5 = ss[0].toByteArray().md5
                if (smd5.hashCode() != -465776148) {
                    exitProcess(0)
                }
            } else {
                exitProcess(0)
            }
        }
    }

    private fun startBroadcastReceivers() {
        runOnNewHandlerThread("startBroadcastReceivers", delay = 2000) {
            PowerEventReceiver.start()
            ScreenStatusListener.start()
            AppInstallReceiver.start()
            UtilEventReceiver.start()
//            BTConnectListener.start()
        }
    }

    private fun launchExtension() {
        if (AppConfig.extPowerIndicator) {
            PowerListener.start()
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun getType(uri: Uri): String? = null
}