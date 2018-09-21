package cn.vove7.jarvis.fragments

import android.content.Intent
import android.os.Handler
import android.view.View
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.NODE_SCOPE_GLOBAL
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.ResponseMessage
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.NewInstActivity
import cn.vove7.jarvis.activities.OnSyncInst
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.parseengine.engine.ParseEngine
import cn.vove7.vtp.log.Vog
import com.google.gson.reflect.TypeToken
import kotlin.concurrent.thread

/**
 *
 * 指令管理列表
 * Created by 17719 on 2018/8/13
 */
class GlobalInstListFragment : SimpleListFragment<ActionNode>(), OnSyncInst {

    var instDetailFragment = InstDetailFragment()
    override var floatClickListener: View.OnClickListener? = View.OnClickListener {
        val intent = Intent(context, NewInstActivity::class.java)
        intent.putExtra("type", NODE_SCOPE_GLOBAL)
        startActivity(intent)
    }
    override val itemClickListener: SimpleListAdapter.OnItemClickListener =
        object : SimpleListAdapter.OnItemClickListener {
            override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
                //显示详情
                val node = item.extra as ActionNode
                instDetailFragment.setInst(node)
                instDetailFragment.show(activity?.supportFragmentManager, "inst_detail")
            }
        }

    override fun onSync() {
        showProgressBar()
        NetHelper.postJson<List<ActionNode>>(ApiUrls.SYNC_GLOBAL_INST, BaseRequestModel(""),
                type = NetHelper.ActionNodeListType) { _, bean ->
            if (bean != null) {
                if (bean.isOk()) {
                    val list = bean.data
                    if (list != null) {
                        DaoHelper.updateGlobalInst(list).also {
                            if(it) {
                                toast.showShort("同步完成")
                                refresh()
                                ParseEngine.updateNode()
                            }
                            else toast.showShort("同步失败")
                        }

                    } else {
                        GlobalLog.err("code: GI57")
                        toast.showShort(R.string.text_error_occurred)
                    }
                } else toast.showShort(R.string.text_net_err)

            } else toast.showShort(R.string.text_net_err)

            hideProgressBar()
        }

    }

    override fun transData(nodes: List<ActionNode>): List<ViewModel> {
        val tmp = mutableListOf<ViewModel>()
        nodes.forEach {
            tmp.add(ViewModel((it).actionTitle, extra = it))
        }
        return tmp
    }

    override fun onGetData(pageIndex: Int) {
        thread {
            val offsetDatas = DAO.daoSession.actionNodeDao.queryBuilder()
                    .where(ActionNodeDao.Properties.ActionScopeType.eq(ActionNode.NODE_SCOPE_GLOBAL))
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