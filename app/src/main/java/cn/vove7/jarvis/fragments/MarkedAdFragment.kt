package cn.vove7.jarvis.fragments

import android.content.Intent
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.datamanager.AppAdInfo
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.greendao.AppAdInfoDao
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.ResponseMessage
import cn.vove7.common.netacc.model.SyncMarkedModel
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.executorengine.helper.AdvanAppHelper
import cn.vove7.jarvis.activities.AppAdListActivity
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.fragments.base.BaseMarkedFragment
import cn.vove7.jarvis.plugins.AdKillerService
import cn.vove7.jarvis.utils.RuntimeConfig
import com.google.gson.reflect.TypeToken
import kotlin.concurrent.thread

/**
 * # MarkedAdFragment
 *
 * @author 17719247306
 * 2018/9/7
 */
class MarkedAdFragment : BaseMarkedFragment<Map<String, ArrayList<AppAdInfo>>>() {

    override val types: Array<String> = arrayOf()
    override val itemClickListener: SimpleListAdapter.OnItemClickListener
        get() = object : SimpleListAdapter.OnItemClickListener {
            override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
                val intent = Intent(context, AppAdListActivity::class.java)
                intent.putExtra("title", item.title)
                intent.putExtra("list", (item.extra as ArrayList<*>))
                startActivity(intent)
            }
        }

    /**
     * 同步广告
     * @param types Array<String>
     */
    override fun onSync(types: Array<String>) {
        showProgressBar()
        val syncPkgs = SyncMarkedModel(packages = AdvanAppHelper.getPkgList())

        NetHelper.postJson<List<AppAdInfo>>(ApiUrls.SYNC_APP_AD, BaseRequestModel(syncPkgs), type = object
            : TypeToken<ResponseMessage<List<AppAdInfo>>>() {}.type) { _, bean ->
            if (bean != null) {
                if (bean.isOk()) {
                    //
                    DaoHelper.updateAppAdInfo(bean.data ?: emptyList())
                    toast.showShort("同步完成")
                    refresh()
                    if (RuntimeConfig.isAdBlockService && AccessibilityApi.isOpen()) {//重启服务
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

    override fun transData(nodes: List<Map<String, ArrayList<AppAdInfo>>>): List<ViewModel> {
        val ss = mutableListOf<ViewModel>()
        val sss = mutableListOf<ViewModel>()
        val bridge = SystemBridge()
        nodes[0].forEach {
            val app = bridge.getAppInfo(it.key)
            if (app != null)
                sss.add(ViewModel(app.name, "数量：${it.value.size}", app.icon, extra = it.value))
        }
        ss.addAll(sss)
        return ss
    }

    override fun clearDataSet() {
        super.clearDataSet()
        maps.clear()
    }

    val maps = mutableMapOf<String, ArrayList<AppAdInfo>>()
    private fun dataOffset(): Int {
        var count = 0
        maps.forEach {
            count += it.value.size
        }
        return count
    }

    override fun onGetData(pageIndex: Int) {
        var full = false
        thread {
            val offMaps = mutableMapOf<String, ArrayList<AppAdInfo>>()
            DAO.daoSession.appAdInfoDao.queryBuilder()
                    .offset(dataOffset())
                    .limit(20 * pageSizeLimit)
                    .orderAsc(AppAdInfoDao.Properties.Pkg)
                    .list().run {
                        forEach {
                            if (offMaps.containsKey(it.pkg)) {
                                offMaps[it.pkg]!!.add(it)
                            } else {
                                if (full) {
                                    return@run
                                }
                                offMaps[it.pkg] = arrayListOf(it)
                            }
                            if (offMaps.size == pageSizeLimit) {//填满
                                full = true
                            }
                        }
                    }
            maps.putAll(offMaps)
            val allLoad = offMaps.size < pageSizeLimit //未填满 allLoad

            dataSet.addAll(transData(arrayListOf(offMaps)))
            if (allLoad)
                resultHandler.sendEmptyMessage(0)
            else resultHandler.sendEmptyMessage(offMaps.size)

        }
    }
}