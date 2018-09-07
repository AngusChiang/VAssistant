package cn.vove7.jarvis.fragments

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedContact
import cn.vove7.common.datamanager.greendao.MarkedContactDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import kotlin.concurrent.thread

/**
 * # MarkedContractFragment
 *
 * @author 17719247306
 * 2018/9/4
 */
class MarkedContractFragment : SimpleListFragment<MarkedContact>() {

    var showServer = false
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
        buildHeader("显示外部数据", lis = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            showServer = isChecked
            refresh()
        })
    }

    override fun transData(nodes: List<MarkedContact>): List<ViewModel> {
        val ss = mutableListOf<ViewModel>()
        nodes.forEach {
            ss.add(ViewModel(it.contactName, it.phone, null, it))
        }
        return ss
    }

    override fun onGetData(pageIndex: Int) {
        thread {
            val builder = DAO.daoSession.markedContactDao
                    .queryBuilder()
                    .offset(pageSizeLimit * pageIndex)
                    .limit(pageSizeLimit)

            if (!showServer) {
                builder.where(MarkedContactDao.Properties.From.eq(DataFrom.FROM_USER))
            }
            val list = builder.list()

            dataSet.addAll(transData(list))
            resultHandler.sendEmptyMessage(list.size)
        }
    }
}