package cn.vove7.jarvis.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.ActionNodeDao
import cn.vove7.common.datamanager.greendao.ActionScopeDao
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.NODE_SCOPE_IN_APP
import cn.vove7.jarvis.activities.NewInstActivity
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.parseengine.engine.ParseEngine
import cn.vove7.vtp.builder.BundleBuilder
import cn.vove7.vtp.log.Vog


/**
 * # InAppInstListFragment
 *
 * @author 17719247306
 * 2018/8/18
 */
class InAppInstListFragment : SimpleListFragment<ActionNode>() {
    lateinit var pkg: String


    override var floatClickListener: View.OnClickListener? = View.OnClickListener {
        if (!AppConfig.checkUser()) {
            return@OnClickListener
        }
        val intent = Intent(context, NewInstActivity::class.java)
        intent.putExtra("type", NODE_SCOPE_IN_APP)
        intent.putExtra("pkg", pkg)
        startActivity(intent)
    }

    override val itemClickListener = object : SimpleListAdapter.OnItemClickListener {
        override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
            val node = item.extra as ActionNode
            InstDetailFragment(node) {
                ParseEngine.updateGlobal()
                refresh()
            }.show(activity?.supportFragmentManager, "inst_detail")
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
            Vog.d(this, "getInstList sIds: $sIds")

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
        Vog.d(this, pkg)
    }

    /**
     * desc activity
     */
    override fun transData(nodes: List<ActionNode>): List<ViewModel> {
        val tmp = mutableListOf<ViewModel>()
        nodes.forEach {
            tmp.add(ViewModel((it).actionTitle, it.actionScope.activity, extra = it))
        }
        return tmp
    }

    override fun onGetData(pageIndex: Int) {
        val offsetDatas = getInstList(pkg)
        Vog.d(this, "onGetData $offsetDatas")
        dataSet.addAll(transData(offsetDatas))
        notifyLoadSuccess(true)

    }

}
