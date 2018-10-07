package cn.vove7.jarvis.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.DaoHelper
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.NODE_SCOPE_GLOBAL
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.InstDetailActivity
import cn.vove7.jarvis.activities.NewInstActivity
import cn.vove7.jarvis.activities.OnSyncInst
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.jarvis.utils.NetHelper
import cn.vove7.parseengine.engine.ParseEngine
import cn.vove7.vtp.sharedpreference.SpHelper
import kotlin.concurrent.thread

/**
 *
 * 指令管理列表
 * Created by 17719 on 2018/8/13
 */
class GlobalInstListFragment : SimpleListFragment<ActionNode>(), OnSyncInst {

    //    var instDetailFragment =
    override var floatClickListener: View.OnClickListener? = View.OnClickListener {
        if (!AppConfig.checkUser()) {
            return@OnClickListener
        }
        val intent = Intent(context, NewInstActivity::class.java)
        intent.putExtra("type", NODE_SCOPE_GLOBAL)
        startActivity(intent)
    }
    override val itemClickListener: SimpleListAdapter.OnItemClickListener =
        object : SimpleListAdapter.OnItemClickListener {
            override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
                //显示详情
                val node = item.extra as ActionNode

                val intent = Intent(context, InstDetailActivity::class.java)
                intent.putExtra("nodeId", node.id)
                startActivity(intent)
//                InstDetailFragment(node) {
//                    ParseEngine.updateGlobal()
//                    refresh()
//                }.show(activity?.supportFragmentManager, "inst_detail")
            }
        }

    var onlySelf = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildHeader("仅显示我的", lis = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            onlySelf = isChecked
            refresh()
        })
    }

    override fun onSync() {
        if (!UserInfo.isLogin()) {
            toast.blue().showShort("请登陆后操作")
            return
        }
        showProgressBar()
        NetHelper.postJson<List<ActionNode>>(ApiUrls.SYNC_GLOBAL_INST, BaseRequestModel(""),
                type = NetHelper.ActionNodeListType) { _, bean ->
            if (bean != null) {
                if (bean.isOk()) {
                    val list = bean.data
                    if (list != null) {
                        DaoHelper.updateGlobalInst(list).also {
                            if (it) {
                                toast.showShort("同步完成")
                                refresh()
                                SpHelper(GlobalApp.APP).set(R.string.key_last_sync_global_date, System.currentTimeMillis())
                                ParseEngine.updateGlobal()
                            } else toast.showShort("同步失败")
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
            val fs = it.follows?.size ?: 0
            tmp.add(ViewModel((it).actionTitle, (it.desc?.instructions ?: "无介绍") +
                    (if (fs == 0) "" else "\n跟随 $fs"), extra = it))
        }
        return tmp
    }

    override fun onGetData(pageIndex: Int) {
        thread {
            val builder = DAO.daoSession.actionNodeDao.queryBuilder()
                    .where(ActionNodeDao.Properties.ActionScopeType.eq(ActionNode.NODE_SCOPE_GLOBAL))

            if (onlySelf) {
                if (onlySelf) {
                    builder.whereOr(ActionNodeDao.Properties.From.eq(DataFrom.FROM_USER),
                            builder.and(ActionNodeDao.Properties.From.eq(DataFrom.FROM_SHARED),
                                    ActionNodeDao.Properties.PublishUserId.eq(UserInfo.getUserId()))
                    )
                }
            }
            val offsetDatas = builder.offset(pageIndex * pageSizeLimit)
                    .limit(pageSizeLimit).list()
            dataSet.addAll(transData(offsetDatas))
            postLoadResult(offsetDatas.isEmpty())
        }
    }


}