package cn.vove7.jarvis.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.model.UserInfo
import cn.vove7.jarvis.activities.AppAdListActivity
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.fragments.base.OnSyncMarked
import cn.vove7.jarvis.plugins.AdKillerService
import cn.vove7.jarvis.tools.DataUpdator
import cn.vove7.jarvis.view.dialog.AdEditorDialog
import cn.vove7.vtp.log.Vog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * # MarkedAdFragment
 *
 * @author 17719247306
 * 2018/9/7
 */
class MarkedAdFragment : SimpleListFragment<String>(), OnSyncMarked {
    /**
     * 所有ad pkg
     */
    private val adAddPkgs = arrayListOf<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        refreshable = false
        updateAdApp()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun updateAdApp() {
        maps.clear()
        adAddPkgs.clear()
        val appInfos = DAO.daoSession.appAdInfoDao.queryBuilder().list()
        val l = mutableSetOf<String>()
        appInfos.forEach {
            l.add(it.pkg)
            if (maps.containsKey(it.pkg)) {
                maps[it.pkg] = (maps[it.pkg])!! + 1
            } else {
                maps[it.pkg] = 1
            }
        }
        adAddPkgs.addAll(l.toList())
    }

    override val itemClickListener = object : SimpleListAdapter.OnItemClickListener<String> {
        override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ListViewModel<String>) {
            val intent = Intent(context, AppAdListActivity::class.java)
            intent.putExtra("title", item.title)
            intent.putExtra("pkg", item.extra)
            startActivity(intent)
        }
    }

    override var floatClickListener: View.OnClickListener? = View.OnClickListener {
        if (!AppConfig.checkLogin()) {
            return@OnClickListener
        }
        AdEditorDialog(activity as BaseActivity<*>) {
            refresh()
            AdKillerService.update()
        }.show()
    }

    /**
     * 同步广告
     * @param types Array<String>
     */
    override fun onSync(types: Array<String>) {
        if (!UserInfo.isLogin()) {
            GlobalApp.toastWarning("请登陆后操作")
            return
        }
        showProgressBar()
        launchIO {
            if (DataUpdator.syncMarkedAd()) {
                withContext(Dispatchers.Main) {
                    refresh()
                }
                GlobalApp.toastSuccess("同步完成")
            }
            withContext(Dispatchers.Main) {
                hideProgressBar()
            }
        }
    }

    override fun transData(nodes: Collection<String>): List<ListViewModel<String>> {
        val ss = mutableListOf<ListViewModel<String>>()
        val sss = mutableListOf<ListViewModel<String>>()
        nodes.forEach {
            val app = SystemBridge.getAppInfo(it)
            if (app != null)
                sss.add(ListViewModel(app.name, "数量：${maps[it]}", app.icon, extra = it))
        }
        ss.addAll(sss)
        return ss
    }

    /**
     * pkg -> int size
     */
    val maps = mutableMapOf<String, Int>()

    override fun onLoadData(pageIndex: Int) {
        launch {
            notifyLoadSuccess(adAddPkgs.sub(pageIndex * pageSizeLimit, pageSizeLimit))
        }
    }
}

fun <T> List<T>.sub(begin: Int, size: Int): List<T> {
    val end = begin + size

    if (begin >= this.size) {
        return emptyList()
    }
    val eee = if (end >= this.size) this.size else end

    Vog.d("sub ---> $begin --> $eee")
    return subList(begin, eee)
}
//            val offMaps = mutableMapOf<String, ArrayList<AppAdInfo>>()
//            DAO.daoSession.appAdInfoDao.queryBuilder()
//                    .offset(dataOffset())
//                    .limit(20 * pageSizeLimit)
//                    .orderAsc(AppAdInfoDao.Properties.Pkg)
//                    .list().run {
//                        forEach {
//                            if (offMaps.containsKey(it.pkg)) {
//                                offMaps[it.pkg]!!.add(it)
//                            } else {
//                                if (full) {
//                                    return@run
//                                }
//                                offMaps[it.pkg] = arrayListOf(it)
//                            }
//                            if (offMaps.size == pageSizeLimit) {//填满
//                                full = true
//                            }
//                        }
//                    }