package cn.vove7.jarvis.fragments

import android.view.*
import cn.vove7.common.utils.ThreadPool
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.droidplugin.PluginManager
import cn.vove7.jarvis.droidplugin.VPluginInfo
import com.afollestad.materialdialogs.MaterialDialog

/**
 * # InstalledPluginFragment
 * 已安装插件列表
 * @author Administrator
 * 2018/11/23
 */
class InstalledPluginFragment : SimpleListFragment<VPluginInfo>() {

    lateinit var pluginManager: PluginManager
    override val itemCheckable: Boolean = true

    companion object {
        fun newInstance(pluginManager: PluginManager): InstalledPluginFragment {
            val f = InstalledPluginFragment()
            f.pluginManager = pluginManager
            return f
        }
    }

    override fun initView(contentView: View) {
        super.initView(contentView)
        registerForContextMenu(recyclerView)
    }

    override fun onItemPopupMenu(item: MenuItem?, pos: Int, viewItem: ViewModel<VPluginInfo>): Boolean {
        when (item?.itemId) {
            12 -> {
                MaterialDialog(context!!)
                        .title(text = "确认卸载")
                        .message(text = viewItem.title)
                        .negativeButton()
                        .show {
                            positiveButton {
                                viewItem.extra.uninstall().also {
                                    if (!it) toast.showShort("卸载完成，重启App后生效")
                                }
                                refresh()
                            }
                        }
            }
            11 -> {
                MaterialDialog(context!!)
                        .title(text = "${viewItem.title} 更新日志")
                        .message(text = viewItem.extra.updateLog ?: "无")
                        .positiveButton()
                        .show()
            }
            10 -> {
                ThreadPool.runOnCachePool {
                    viewItem.extra.launch()
                }
            }
        }

        return super.onItemPopupMenu(item, pos, viewItem)
    }

    override fun onCreatePopupMenu(menu: ContextMenu, pos: Int, viewItem: ViewModel<VPluginInfo>) {
//        menu.addSubMenu(0, 0, 0, viewItem.extra.name ?: "...")
        menu.add(0, 10, 1, "启动")
        menu.add(0, 11, 1, "更新日志")
        menu.add(0, 12, 2, "卸载")
    }

    override val itemClickListener = object : SimpleListAdapter.OnItemClickListener<VPluginInfo> {
        override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel<VPluginInfo>) {
            item.extra.launch()
        }

        override fun onLongClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel<VPluginInfo>): Boolean {
            //pop菜单
            return false
//            (item.extra as VPluginInfo).uninstall()also {
//                if (!it) toast.showShort("卸载完成，重启App后生效")
//            }
// pluginManager.launchPluginMainActivity(item.extra as VPluginInfo).also {
//                            if (!it) toast.showShort("启动失败")
//                        }
        }

        override fun onItemCheckedStatusChanged(holder: SimpleListAdapter.VHolder?, item: ViewModel<VPluginInfo>, isChecked: Boolean) {
            item.extra.apply {
                enabled = isChecked
                if (isChecked) startService()
                else stopService()
            }
        }
    }

    override fun onGetData(pageIndex: Int) {
        notifyLoadSuccess(pluginManager.installList(true), true)
    }
}