package cn.vove7.jarvis.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.greendao.ActionScopeDao
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.NODE_SCOPE_IN_APP
import cn.vove7.jarvis.activities.InstDetailActivity
import cn.vove7.jarvis.activities.NewInstActivity
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.common.app.AppConfig
import cn.vove7.vtp.builder.BundleBuilder
import cn.vove7.vtp.log.Vog


/**
 * # InAppInstListFragment
 *
 * @author Vove
 * 2018/8/18
 */
class InAppInstListFragment : SimpleListFragment<ActionNode>() {
    lateinit var pkg: String

    override var floatClickListener: View.OnClickListener? = View.OnClickListener {
        if (!AppConfig.checkLogin()) {
            return@OnClickListener
        }
        val intent = Intent(context, NewInstActivity::class.java)
        intent.putExtra("type", NODE_SCOPE_IN_APP)
        intent.putExtra("pkg", pkg)
        startActivity(intent)
    }

    override val itemClickListener = object : SimpleListAdapter.OnItemClickListener<ActionNode> {
        override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ListViewModel<ActionNode>) {
            val node = item.extra

            val intent = Intent(context, InstDetailActivity::class.java)
            intent.putExtra("nodeId", node.id)
            startActivityForResult(intent, 1)
//            InstDetailFragment(node) {
//                ParseEngine.updateInApp()
//                refresh()
//            }.show(activity?.supportFragmentManager, "inst_detail")
        }
    }

    companion object {
        fun newInstance(pkg: String?, title: String?): InAppInstListFragment {
            val f = InAppInstListFragment()
            f.arguments = BundleBuilder()
                    .put("pkg", pkg ?: "")
                    .put("title", title ?: "")
                    .data
            return f
        }

        /**
         * 通过pkg获取App内指令
         * @param pkg String
         * @return List<ActionNode>
         */
        fun getInstList(pkg: String): List<ActionNode> {
            //获取pkg对应scopeIds
            val sIds = mutableListOf<Long>()
            DAO.daoSession.actionScopeDao.queryBuilder()
                    .where(ActionScopeDao.Properties.PackageName.eq(pkg))
                    .list().forEach {
                        sIds.add(it.id)
                    }
            Vog.d("sIds: $sIds")

            return DAO.daoSession.actionNodeDao.queryBuilder()
                    .where(ActionNodeDao.Properties.ActionScopeType.eq(ActionNode.NODE_SCOPE_IN_APP/*, ActionNode.NODE_SCOPE_IN_APP_2*/))
                    .where(ActionNodeDao.Properties.ScopeId.`in`(sIds))
                    .list()
//                    .filter { it.actionScope.packageName == pkg }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pkg = arguments?.getString("pkg") ?: ""
        arguments?.getString("title").also {
            if (it != null && it != "") activity?.title = it
        }
        Vog.d(pkg)
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

    override fun unification(it: ActionNode): ListViewModel<ActionNode>? {
        return ListViewModel((it).actionTitle, (it.desc?.instructions ?: "无介绍"), extra = it)
    }

    override fun onLoadData(pageIndex: Int) {
        val offsetDatas = getInstList(pkg)
        Vog.d(offsetDatas.toString())
        notifyLoadSuccess(offsetDatas, true)
    }

}
