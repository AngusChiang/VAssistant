package cn.vove7.jarvis.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.NODE_SCOPE_GLOBAL
import cn.vove7.common.model.UserInfo
import cn.vove7.jarvis.activities.InstDetailActivity
import cn.vove7.jarvis.activities.NewInstActivity
import cn.vove7.jarvis.activities.OnSyncInst
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.tools.DataUpdator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *
 * 指令管理列表
 * Created by 17719 on 2018/8/13
 */
class GlobalInstListFragment : SimpleListFragment<ActionNode>(), OnSyncInst {

    //    var instDetailFragment =
    override var floatClickListener: View.OnClickListener? = View.OnClickListener {
        if (!AppConfig.checkLogin()) {
            return@OnClickListener
        }
        val intent = Intent(context, NewInstActivity::class.java)
        intent.putExtra("type", NODE_SCOPE_GLOBAL)
        startActivity(intent)
    }
    override val itemClickListener: SimpleListAdapter.OnItemClickListener<ActionNode> =
            object : SimpleListAdapter.OnItemClickListener<ActionNode> {
                override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ListViewModel<ActionNode>) {
                    //显示详情
                    val node = item.extra
                    val intent = Intent(context, InstDetailActivity::class.java)
                    intent.putExtra("nodeId", node.id)
                    startActivityForResult(intent, 1)
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
            GlobalApp.toastWarning("请登陆后操作")
            return
        }
        showProgressBar()

        launchIO {
            if(DataUpdator.syncGlobalInst()){
                GlobalApp.toastSuccess("同步完成")
                withContext(Dispatchers.Main) {
                    refresh()
                }
            }
            withContext(Dispatchers.Main){
                hideProgressBar()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            when (data.getStringExtra("action")) {
                "del" -> {
                    val id = data.getLongExtra("id", -1)
                    dataSet.find { it.extra.id == id }?.also {
                        dataSet.remove(it)
                    }
                    notifyDataSetChanged()
                }
                "update" -> {
                    val id = data.getLongExtra("id", -1)
                    dataSet.find { it.extra.id == id }?.also {
                        it.extra.refresh()
                    }
                    notifyDataSetChanged()
                }
                "insert" -> {
                    refresh()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun unification(data: ActionNode): ListViewModel<ActionNode>? {
        return ListViewModel((data).actionTitle, (data.desc?.instructions ?: "无介绍"), extra = data)
    }

    override fun onLoadData(pageIndex: Int) {
        launch {
            val builder = DAO.daoSession.actionNodeDao.queryBuilder()
                    .where(ActionNodeDao.Properties.ActionScopeType.eq(ActionNode.NODE_SCOPE_GLOBAL))
                    .orderDesc(ActionNodeDao.Properties.Priority)//按优先级
            if (onlySelf) {
                builder.whereOr(ActionNodeDao.Properties.From.eq(DataFrom.FROM_USER),
                        builder.and(ActionNodeDao.Properties.From.eq(DataFrom.FROM_SHARED),
                                ActionNodeDao.Properties.PublishUserId.eq(UserInfo.getUserId()))
                )
            }
            val offsetDatas = builder.offset(pageIndex * pageSizeLimit)
                    .limit(pageSizeLimit).list()
            notifyLoadSuccess(offsetDatas)
        }
    }


}