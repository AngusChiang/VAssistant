package cn.vove7.jarvis.fragments

import android.content.Intent
import android.os.Handler
import android.view.View
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.jarvis.activities.NewInstActivity
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.vtp.log.Vog
import kotlin.concurrent.thread

/**
 *
 * 指令管理列表
 * Created by 17719 on 2018/8/13
 */

class GlobalInstListFragment : SimpleListFragment<ActionNode>() {

    var instDetailFragment = InstDetailFragment()
    override var floatClickListener: View.OnClickListener? = View.OnClickListener {
        val intent = Intent(context, NewInstActivity::class.java)
        intent.putExtra("type", NewInstActivity.TYPE_GLOBAL)
        startActivity(intent)
    }
    override val itemClickListener: SimpleListAdapter.OnItemClickListener =
        object : SimpleListAdapter.OnItemClickListener {
            override fun onItemClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
                //显示详情
                val node = item.extra as ActionNode
                instDetailFragment.setInst(node)
                instDetailFragment.show(activity?.supportFragmentManager, "inst_detail")
            }
        }


    companion object {

        fun newInstance(): GlobalInstListFragment {
            return GlobalInstListFragment()
        }
    }

    override fun transData(nodes: List<ActionNode>): List<ViewModel> {
        val tmp = mutableListOf<ViewModel>()
        nodes.forEach {
            tmp.add(ViewModel((it).descTitle, extra = it))
        }
        return tmp
    }

    override fun onGetData(pageIndex: Int) {
        super.onGetData(pageIndex)
        thread {
            val offsetDatas = DAO.daoSession.actionNodeDao.queryBuilder()
                    .where(ActionNodeDao.Properties.NodeType.eq(ActionNode.NODE_TYPE_GLOBAL))
                    .offset(pageIndex * pageSizeLimit)
                    .limit(pageSizeLimit).list()
            Vog.d(this, "onGetData $offsetDatas")
            dataSet.addAll(transData(offsetDatas))
            allLoadFlag = offsetDatas.isEmpty()
            handler.sendEmptyMessage(0)
        }
    }

    val handler = Handler {
        notifyLoadSuccess(allLoadFlag)
        return@Handler true
    }

}