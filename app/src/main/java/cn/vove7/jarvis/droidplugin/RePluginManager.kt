package cn.vove7.jarvis.droidplugin

import android.content.Intent
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.vtp.file.FileHelper
import cn.vove7.vtp.log.Vog
import com.qihoo360.replugin.RePlugin
import com.qihoo360.replugin.component.service.PluginServiceClient
import java.io.File

/**
 * # RePluginManager
 *
 * 插件提供接口：采用反射机制
 * plugin host 通讯方式：
 * 1. host -> plugin Service.onStartCommand 服务|广播
 * 2. plugin -> host  广播|反射
 * plugin 端 hook host
 *
 * 插件MainService 自启申请权限：LAUNCH_WITH_APP
 * @author Administrator
 * 2018/11/19
 */
class RePluginManager : PluginManager {

    override fun installPlugin(path: String): VPluginInfo? {//先拷贝
        val f = File(path)
        return try {
            val desf = File(context.cacheDir.absoluteFile, f.name)
            FileHelper.easyCopy(f, desf)
            RePlugin.install(desf.absolutePath).let {
                if (it == null) {
                    GlobalLog.err("插件安装失败 ${desf.absolutePath}")
                    null
                } else {
                    Vog.d("安装成功 ${desf.absolutePath}")
                    RePluginInfo(it)
                }
            }
        } catch (e: Throwable) {
            GlobalApp.toastError("插件安装失败，详情见日志")
            GlobalLog.err(e)
            null
        }
    }

    override fun uninstallPlugin(pluginInfo: VPluginInfo): Boolean {
        Vog.d("卸载${pluginInfo.name}")
        return try {
            RePlugin.uninstall(pluginInfo.packageName)
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }

    override fun launchPluginMainActivity(pluginInfo: VPluginInfo): Boolean {
        if (pluginInfo.mainActivity == null) {
            GlobalApp.toastWarning("无用户界面")
            return false
        }
        return try {
            RePlugin.startActivity(context, RePlugin
                    .createIntent(pluginInfo.packageName, pluginInfo.mainActivity).also {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                    })
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }

//    override fun killAll() {
//        RePlugin.getRunningPlugins().forEach {
//            android.os.Process.killProcess(IPC.getPidByProcessName(it))
//        }
//    }
//
//    override fun kill(pluginInfo: VPluginInfo) {
//        RePlugin.getRunningProcessesByPlugin(pluginInfo.packageName)?.forEach {
//            android.os.Process.killProcess(IPC.getPidByProcessName(it))
//
//        }
//    }

    override fun getInfo(pkg: String?): VPluginInfo? {
        if (pkg == null) return null
        return RePlugin.getPluginInfo(pkg)?.let {
            RePluginInfo(it)
        }
    }

    override fun stopPluginService(pluginInfo: VPluginInfo): Boolean {
        if (pluginInfo.mainService == null) {
            Vog.d("${pluginInfo.name} 无主服务")
            return false
        }
        return try {
            PluginServiceClient.stopService(context, RePlugin
                    .createIntent(pluginInfo.packageName, pluginInfo.mainService))
            true
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }

    override fun startPluginService(pluginInfo: VPluginInfo): Boolean {
        if (pluginInfo.mainService == null) {
            Vog.d("${pluginInfo.name} 无主服务")
            return false
        }
        return try {
            PluginServiceClient.startService(context, RePlugin
                    .createIntent(pluginInfo.packageName, pluginInfo.mainService))
            true
        } catch (e: Exception) {
            GlobalLog.err(e)
            false
        }
    }

    override var cacheList: MutableList<VPluginInfo> = mutableListOf()

    override fun installList(refresh: Boolean): List<VPluginInfo> {
        if (cacheList.isNotEmpty() && !refresh) {
            return cacheList
        }
        cacheList.clear()
        RePlugin.getPluginInfoList().forEach {
            cacheList.add(RePluginInfo(reInfo = it))
        }
        Vog.d("插件数量${cacheList.size}")
        return cacheList
    }
}
