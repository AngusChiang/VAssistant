package cn.vove7.jarvis.fragments

import android.content.Intent
import android.view.View
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.datamanager.AppAdInfo
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.utils.NetHelper
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.ResponseMessage
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.jarvis.activities.AppAdListActivity
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.fragments.base.OnSyncMarked
import cn.vove7.jarvis.plugins.AdKillerService
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.jarvis.view.dialog.AdEditorDialog
import cn.vove7.vtp.log.Vog
import com.google.gson.reflect.TypeToken
import kotlin.concurrent.thread

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
    val adAddPkgs = arrayListOf<String>()

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

    override fun clearDataSet() {
        super.clearDataSet()
        updateAdApp()
    }

    override val itemClickListener = object : SimpleListAdapter.OnItemClickListener {
        override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
            val intent = Intent(context, AppAdListActivity::class.java)
            intent.putExtra("title", item.title)
            intent.putExtra("pkg", (item.extra as String))
            startActivity(intent)
        }
    }

    override var floatClickListener: View.OnClickListener? = View.OnClickListener {
        if (!AppConfig.checkUser()) {
            return@OnClickListener
        }
        AdEditorDialog(context!!) { refresh() }.show()
    }

    /**
     * 同步广告
     * @param types Array<String>
     */
    override fun onSync(types: Array<String>) {
        showProgressBar()
        val syncPkgs = AdvanAppHelper.getPkgList()

        NetHelper.postJson<List<AppAdInfo>>(ApiUrls.SYNC_APP_AD, BaseRequestModel(syncPkgs), type = object
            : TypeToken<ResponseMessage<List<AppAdInfo>>>() {}.type) { _, bean ->
            if (bean != null) {
                if (bean.isOk()) {
                    //
                    DaoHelper.updateAppAdInfo(bean.data ?: emptyList())
                    toast.showShort("同步完成")
                    AdKillerService.update()
                    refresh()
                    if (AppConfig.isAdBlockService && AccessibilityApi.isOpen()) {//重启服务
                        AdKillerService.restart()
                    }
                } else {
                    toast.showShort(bean.message)
                }
            } else {
                toast.showShort("出错")
            }
            hideProgressBar()
        }
    }

    override fun transData(nodes: List<String>): List<ViewModel> {
        val ss = mutableListOf<ViewModel>()
        val sss = mutableListOf<ViewModel>()
        val bridge = SystemBridge()
        nodes.forEach {
            val app = bridge.getAppInfo(it)
            if (app != null)
                sss.add(ViewModel(app.name, "数量：${maps[it]}", app.icon, extra = it))
        }
        ss.addAll(sss)
        return ss
    }

    /**
     * pkg -> int size
     */
    val maps = mutableMapOf<String, Int>()

    override fun onGetData(pageIndex: Int) {
        thread {
            val subSet = adAddPkgs.sub(pageIndex * pageSizeLimit, pageSizeLimit)
            dataSet.addAll(transData(subSet))
            resultHandler.sendEmptyMessage(subSet.size)
        }
    }
}

fun <T> List<T>.sub(begin: Int, size: Int): List<T> {
    val end = begin + size

    if (begin >= this.size) {
        return emptyList()
    }
    val eee = if (end >= this.size) this.size else end

    Vog.d(this, "sub ---> $begin --> $eee")
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