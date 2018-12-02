package cn.vove7.jarvis.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuItem
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivityWithViewPager
import cn.vove7.jarvis.droidplugin.PluginManager
import cn.vove7.jarvis.droidplugin.RePluginManager
import cn.vove7.jarvis.fragments.InstalledPluginFragment
import cn.vove7.jarvis.fragments.NotInstalledPluginFragment
import cn.vove7.jarvis.fragments.SimpleListFragment
import cn.vove7.jarvis.tools.AppNotification
import cn.vove7.jarvis.tools.UriUtils
import cn.vove7.jarvis.view.dialog.ProgressTextDialog
import cn.vove7.vtp.app.AppHelper
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog
import com.qihoo360.replugin.model.PluginInfo
import org.greenrobot.eventbus.Subscribe

/**
 * # PluginManagerActivity
 * 插件管理
 *
 * @author Administrator
 * 2018/11/18
 */
class PluginManagerActivity : BaseActivityWithViewPager() {

    override var titles: Array<String> = arrayOf("已安装", "未安装")

    var info: PluginInfo? = null
    private val pluginManager: PluginManager = RePluginManager()

    override var fragments: Array<Fragment> = arrayOf(
            InstalledPluginFragment.newInstance(pluginManager),
            NotInstalledPluginFragment.newInstance(pluginManager)
    )

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add("重启App")
        if (BuildConfig.DEBUG)
            menu?.add("安装测试插件")
        menu?.add("从本地安装")
        menu?.add("帮助")
        return true
    }

    override fun onResume() {
        super.onResume()
        AppBus.reg(this)
    }

    @Subscribe
    fun onC(c: String) {
        if (c in arrayOf(AppBus.EVENT_PLUGIN_INSTALLED)) {
            (fragments[0] as SimpleListFragment<*>).refresh()
        }
    }

    override fun onStop() {
        super.onStop()
        AppBus.unreg(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        when (item?.title) {
            "重启App" -> restartApp()
            "安装测试插件" -> installPlugin("/sdcard/test.apk")
                "从本地安装" -> {
                val selIntent = Intent(Intent.ACTION_GET_CONTENT)
                selIntent.type = "*/*"
                selIntent.addCategory(Intent.CATEGORY_OPENABLE)
                try {
                    startActivityForResult(selIntent, 1)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            "帮助" -> {
                ProgressTextDialog(this,"帮助").apply {
                    appendlnBold("功能")
                    appendln("动态扩展更多功能")
                    appendln()
                    appendlnBold("如何使用")
                    appendln("1. 下载安装插件后，勾选插件即可启动插件服务，并且跟随App启动而启动")
                    appendln("2. 取消勾选即可禁用插件服务")
                    appendln("注. 卸载或升级插件可能需要重启App才可生效")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun restartApp() {
        AppHelper.showPackageDetail(this, packageName)
//        pluginManager.killAll()
//        android.os.Process.killProcess(android.os.Process.myPid())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {//选择文件回调
            when (requestCode) {
                1 -> {
                    val uri = data?.data
                    if (uri != null) {
                        try {
                            UriUtils.getPathFromUri(this, uri)?.also {

                                installPlugin(it)
                            }
                        } catch (e: Exception) {
                            GlobalLog.err(e)
                        }
                    }
                }
                else -> {
                }
            }
        }
    }

    companion object {
        fun installPlugin(path: String) {
            RePluginManager().installPlugin(path).also {
                Vog.d(this, "installPlugin ---> $path")
                if (it != null) {
                    Vog.d(this, "installPlugin ---> 插件安装成功")
                    AppNotification.newNotification("插件安装成功", it.name, R.drawable.ic_done)
                    GlobalApp.toastLong("插件安装成功")
                    AppBus.post(AppBus.EVENT_PLUGIN_INSTALLED)
                } else {
                    Vog.d(this, "installPlugin ---> 插件安装失败")
                    AppNotification.newNotification("插件安装失败", "详情见日志", R.drawable.ic_error_black_24dp)
                    GlobalApp.toastLong("插件安装失败，详情见日志")
                }
            }
        }

    }
}