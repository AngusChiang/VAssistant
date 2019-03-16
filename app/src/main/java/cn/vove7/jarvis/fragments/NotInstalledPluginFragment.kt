package cn.vove7.jarvis.fragments

import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.interfaces.DownloadInfo
import cn.vove7.common.interfaces.DownloadProgressListener
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.utils.StorageHelper
import cn.vove7.common.view.editor.MultiSpan
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.PluginManagerActivity
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.droidplugin.PluginManager
import cn.vove7.jarvis.droidplugin.RePluginInfo
import cn.vove7.jarvis.droidplugin.VPluginInfo
import cn.vove7.jarvis.view.dialog.LoadingDialog
import cn.vove7.jarvis.view.dialog.ProgressTextDialog
import cn.vove7.vtp.log.Vog
import com.liulishuo.okdownload.DownloadTask
import java.io.File

/**
 * # NotInstalledPluginFragment
 *
 * @author Administrator
 * 2018/11/23
 */
class NotInstalledPluginFragment : SimpleListFragment<VPluginInfo>() {

    lateinit var pluginManager: PluginManager

    companion object {
        fun newInstance(pluginManager: PluginManager): NotInstalledPluginFragment {
            val f = NotInstalledPluginFragment()
            f.pluginManager = pluginManager
            return f
        }
    }

    override fun unification(data: VPluginInfo): ListViewModel<VPluginInfo>? {
        return if (data.isShow(1))//有更新
            if (data.isInstalled)
                ListViewModel(data.name + " (可更新)", data.subTitle, extra = data)
            else ListViewModel(data.name, data.subTitle, extra = data)
        else null
    }

    override fun onLoadData(pageIndex: Int) {
        NetHelper.postJson<List<RePluginInfo>>(ApiUrls.PLUGIN_LIST) { _, b ->
            if (b?.isOk() == true) {
                notifyLoadSuccess(b.data ?: emptyList())
            } else {
                GlobalLog.err(b?.message)
                GlobalApp.toastError("获取失败")
                failedToLoad()
            }
        }
    }


    var downloadTask: DownloadTask? = null
    override val itemClickListener =
        object : SimpleListAdapter.OnItemClickListener<VPluginInfo> {
            override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ListViewModel<VPluginInfo>) {
                ProgressTextDialog(context!!, item.title).show {
                    positiveButton(text = "下载") {
                        downloadTask = NetHelper.download(ApiUrls.DL_PLUGIN + "${item.extra.fileName}", StorageHelper.pluginsPath,
                                item.extra.fileName ?: (item.title + ".apk"),
                                data = item.title, listener = lis)
                    }

                    appendln(MultiSpan(context, item.extra.description
                        ?: "", colorId = R.color.primary_text).spanStr)
                    appendln()
                    appendln(MultiSpan(context, "作者：${item.extra.author}", fontSize = 13, typeface = Typeface.BOLD_ITALIC).spanStr)
                    appendln(MultiSpan(context, "版本：${item.extra.versionName}", fontSize = 13, typeface = Typeface.BOLD_ITALIC).spanStr)

                    appendln()
                    appendln(MultiSpan(context, "更新日志", fontSize = 15, typeface = Typeface.BOLD).spanStr)
                    appendln(item.extra.updateLog ?: "无")
                }
            }
        }

    val lis: DownloadProgressListener by lazy {
        object : DownloadProgressListener {
            var dialog: LoadingDialog? = null

            fun getName(info: DownloadInfo<*>): String = info.data as String? ?: "未知"

            override fun onSuccess(info: DownloadInfo<*>, file: File) {
                Vog.d("onSuccess ---> $file")
                Handler(Looper.getMainLooper()).postDelayed( {
                    dialog?.message = "下载完成"
                    dialog?.progress = 100
                    dialog?.finish("安装") {
                        PluginManagerActivity.installPlugin(file.absolutePath)
                    }
                },800)
            }

            override fun onStart(info: DownloadInfo<*>) {
                dialog = LoadingDialog(context!!, "${getName(info)} 下载中", horizontal = true)
                        .negativeButton(text = "取消") {
                            downloadTask?.cancel()
                        }.onDismiss {
                            dialog = null
                        } as LoadingDialog

                dialog?.show()
            }

            override fun onDownloading(info: DownloadInfo<*>, progress: Int) {
                dialog?.progress = progress
                dialog?.message = "$progress%"
//                if (!dialog.isShowing) dialog.show()
//                notificationHelper.notifyDownloadProgress(info.id, "正在下载插件", getName(info),
//                        100, progress, null)
            }

            override fun onFailed(info: DownloadInfo<*>, e: Exception) {
                dialog?.message = "下载出错 ${e.message}"
                dialog?.finish()
//                notificationHelper.removeAll()
//                notificationHelper.showNotification(info.id, "插件下载失败", getName(info),
//                        NotificationIcons(android.R.drawable.stat_notify_error))
            }
        }

    }
}
