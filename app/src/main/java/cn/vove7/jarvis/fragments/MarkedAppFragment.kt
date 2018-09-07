package cn.vove7.jarvis.fragments

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedOpen
import cn.vove7.common.datamanager.executor.entity.MarkedOpen.MARKED_TYPE_APP
import cn.vove7.common.datamanager.greendao.MarkedOpenDao
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import kotlin.concurrent.thread

/**
 * # MarkedAppFragment
 *
 * @author 17719247306
 * 2018/9/7
 */
class MarkedAppFragment : SimpleListFragment<MarkedOpen>() {

    var showUninstall = false
    override val itemClickListener = object : SimpleListAdapter.OnItemClickListener {
        override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
            //dialog edit
        }

        override fun onLongClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel): Boolean {
            //batch

            return true
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildHeader("显示未安装应用", lis = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            showUninstall = isChecked
            refresh()
        })
    }

    override fun transData(nodes: List<MarkedOpen>): List<ViewModel> {
        val ss = mutableListOf<ViewModel>()
        val sss = mutableListOf<ViewModel>()
        nodes.forEach {
            val app = SystemBridge().getAppInfo(it.value)
            if (app == null) {
                if (showUninstall)
                    sss.add(ViewModel(it.key, it.value, null, it))
            } else ss.add(ViewModel(it.key, app.name, app.icon, it))
        }
        ss.addAll(sss)
        return ss
    }

    override fun onGetData(pageIndex: Int) {
        thread {
            val builder = DAO.daoSession.markedOpenDao
                    .queryBuilder()
                    .where(MarkedOpenDao.Properties.Type.eq(MARKED_TYPE_APP))
                    .offset(pageSizeLimit * pageIndex)
                    .limit(pageSizeLimit)

            val list = builder.list()

            dataSet.addAll(transData(list))
            resultHandler.sendEmptyMessage(list.size)
        }
    }
}