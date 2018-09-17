package cn.vove7.jarvis.fragments

import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.executor.entity.MarkedData.MARKED_TYPE_CONTACT
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.fragments.base.BaseMarkedFragment
import kotlin.concurrent.thread

/**
 * # MarkedOpenFragment
 *
 * @author 17719247306
 * 2018/9/7
 */
class MarkedOpenFragment : BaseMarkedFragment<MarkedData>() {
    override val types: Array<String> = arrayOf(MarkedData.MARKED_TYPE_SCRIPT_JS, MarkedData.MARKED_TYPE_SCRIPT_LUA)

    override val itemClickListener = object : SimpleListAdapter.OnItemClickListener {
        override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
            //dialog edit
        }

        override fun onLongClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel): Boolean {
            //batch edit

            return true
        }
    }

    override fun transData(nodes: List<MarkedData>): List<ViewModel> {
        val ss = mutableListOf<ViewModel>()
        nodes.forEach {
            ss.add(ViewModel(it.key, null, extra = it))
        }
        return ss
    }

    override fun onGetData(pageIndex: Int) {
        thread {
            val builder = DAO.daoSession.markedDataDao
                    .queryBuilder()
                    .where(MarkedDataDao.Properties.Type.notIn(MarkedData.MARKED_TYPE_APP, MARKED_TYPE_CONTACT))
                    .offset(pageSizeLimit * pageIndex)
                    .limit(pageSizeLimit)

            val list = builder.list()

            dataSet.addAll(transData(list))
            resultHandler.sendEmptyMessage(list.size)
        }
    }
}
