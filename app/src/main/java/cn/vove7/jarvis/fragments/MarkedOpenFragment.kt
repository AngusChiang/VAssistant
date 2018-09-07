package cn.vove7.jarvis.fragments

import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedOpen
import cn.vove7.common.datamanager.greendao.MarkedOpenDao
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import kotlin.concurrent.thread

/**
 * # MarkedOpenFragment
 *
 * @author 17719247306
 * 2018/9/7
 */
class MarkedOpenFragment : SimpleListFragment<MarkedOpen>() {

    override val itemClickListener = object : SimpleListAdapter.OnItemClickListener {
        override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
            //dialog edit
        }

        override fun onLongClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel): Boolean {
            //batch edit

            return true
        }
    }


    override fun transData(nodes: List<MarkedOpen>): List<ViewModel> {
        val ss = mutableListOf<ViewModel>()
        nodes.forEach {
            ss.add(ViewModel(it.key, null, extra = it))
        }
        return ss
    }

    override fun onGetData(pageIndex: Int) {
        thread {
            val builder = DAO.daoSession.markedOpenDao
                    .queryBuilder()
                    .where(MarkedOpenDao.Properties.Type.notIn(MarkedOpen.MARKED_TYPE_APP))
                    .offset(pageSizeLimit * pageIndex)
                    .limit(pageSizeLimit)

            val list = builder.list()

            dataSet.addAll(transData(list))
            resultHandler.sendEmptyMessage(list.size)
        }
    }
}
