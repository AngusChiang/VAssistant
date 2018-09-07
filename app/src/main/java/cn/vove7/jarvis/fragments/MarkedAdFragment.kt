package cn.vove7.jarvis.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import cn.vove7.common.datamanager.AppAdInfo
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.AppAdInfoDao
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.activities.AppAdListActivity
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import java.io.Serializable
import kotlin.concurrent.thread

/**
 * # MarkedAdFragment
 *
 * @author 17719247306
 * 2018/9/7
 */
class MarkedAdFragment : SimpleListFragment<Map<String, ArrayList<AppAdInfo>>>() {

    override val itemClickListener: SimpleListAdapter.OnItemClickListener?
        get() = object : SimpleListAdapter.OnItemClickListener {
            override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
                val intent = Intent(context, AppAdListActivity::class.java)
                intent.putExtra("title", item.title)
                intent.putExtra("list", (item.extra as ArrayList<AppAdInfo>))
                startActivity(intent)


            }
        }
    var showUninstall = false
    override fun transData(nodes: List<Map<String, ArrayList<AppAdInfo>>>): List<ViewModel> {
        val ss = mutableListOf<ViewModel>()
        val sss = mutableListOf<ViewModel>()
        val bridge = SystemBridge()
        nodes[0].forEach {
            val app = bridge.getAppInfo(it.key)
            if (app == null) {
                if (showUninstall)
                    sss.add(ViewModel(it.key, "未安装", extra = it.value))
            } else sss.add(ViewModel(app.name, "数量：${it.value.size}", app.icon, extra = it.value))

        }
        ss.addAll(sss)
        return ss
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildHeader("显示未安装应用", lis = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            showUninstall = isChecked
            refresh()
        })
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