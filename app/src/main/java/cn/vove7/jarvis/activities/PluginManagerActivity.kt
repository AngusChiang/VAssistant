package cn.vove7.jarvis.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import cn.vove7.jarvis.droidplugin.PluginManager
import cn.vove7.jarvis.droidplugin.RePluginManager
import cn.vove7.jarvis.droidplugin.VPluginInfo
import cn.vove7.common.utils.ThreadPool
import cn.vove7.jarvis.activities.base.OneFragmentActivity
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.fragments.SimpleListFragment
import com.qihoo360.replugin.model.PluginInfo

/**
 * # PluginManagerActivity
 * 插件管理
 *
 * @author Administrator
 * 2018/11/18
 */
class PluginManagerActivity : OneFragmentActivity() {
    companion object {
        val storePath = "/sdcard/test.apk"
    }

    var info: PluginInfo? = null
    val pluginManager: PluginManager = RePluginManager()
    override var fragments: Array<Fragment> = arrayOf(ListFragment.newInstance(pluginManager))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ThreadPool.runOnCachePool {
            pluginManager.installPlugin(storePath)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class ListFragment : SimpleListFragment<VPluginInfo>() {
        //检查下载
        lateinit var pluginManager: PluginManager

        companion object {
            fun newInstance(pluginManager: PluginManager): ListFragment {
                val f = ListFragment()
                f.pluginManager = pluginManager
                return f
            }
        }

        override val itemClickListener = object : SimpleListAdapter.OnItemClickListener {
            override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
                pluginManager.launchPluginMainActivity(context!!, item.extra as VPluginInfo).also {
                    if (!it) toast.showShort("启动失败")
                }
            }

            var f = true
            override fun onLongClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel): Boolean {
                if (f) {
                    pluginManager.startPluginService(context!!, item.extra as VPluginInfo).also {
                        if (!it) toast.showShort("启动服务失败")
                    }
                } else {
                    pluginManager.stopPluginService(context!!, item.extra as VPluginInfo).also {
                        if (!it) toast.showShort("关闭服务失败")
                    }
                }
                f = !f
                return true
            }
        }

        override fun unification(data: VPluginInfo): ViewModel {
            return ViewModel(data.name, data.description + "\n" + data.versionName, extra = data)
        }

        override fun onGetData(pageIndex: Int) {
            dataSet.addAll(transData(
                    pluginManager.installList(true)
            ))
            notifyLoadSuccess(true)
        }
    }
}