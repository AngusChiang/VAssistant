package cn.vove7.jarvis.fragments

import android.content.Intent
import android.view.View
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.NODE_SCOPE_IN_APP
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.jarvis.activities.InAppInstActivity
import cn.vove7.jarvis.activities.NewInstActivity
import cn.vove7.jarvis.activities.OnSyncInst
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.DataUpdator
import cn.vove7.vtp.app.AppHelper
import kotlin.concurrent.thread

/**
 * # InstAppListFragment
 * App支持列表
 * @author 17719247306
 * 2018/8/18
 */
class InstAppListFragment : SimpleListFragment<ActionScope>(), OnSyncInst {
    override var floatClickListener: View.OnClickListener? = View.OnClickListener {
        if (!AppConfig.checkLogin()) {
            return@OnClickListener
        }
        val intent = Intent(context, NewInstActivity::class.java)
        intent.putExtra("type", NODE_SCOPE_IN_APP)

        startActivity(intent)
    }

    override val itemClickListener: SimpleListAdapter.OnItemClickListener = object : SimpleListAdapter.OnItemClickListener {
        override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
            val intent = Intent(context, InAppInstActivity::class.java)
            intent.putExtra("pkg", item.extra as String)
            intent.putExtra("title", item.title)
            startActivity(intent)
        }
    }

    override fun onSync() {
        if (!UserInfo.isLogin()) {
            toast.blue().showShort("请登陆后操作")
            return
        }
        showProgressBar()
        DataUpdator.syncInAppInst {
            hideProgressBar()
            if (it) {
                toast.showShort("同步完成")
                refresh()
            }
        }
    }

    /**
     * app sum
     */
    private val pkgSet = hashSetOf<String>()

    override fun clearDataSet() {
        super.clearDataSet()
        pkgSet.clear()
    }

    override fun transData(nodes: List<ActionScope>): List<ViewModel> {
        val tmp = mutableListOf<ViewModel>()
//        val notInstalled = mutableListOf<ViewModel>()
        kotlin.run breaking@{
            nodes.forEach goon@{
                if (pkgSet.contains(it.packageName))
                    return@goon
                val app = AppHelper.getAppInfo(GlobalApp.APP, "", it.packageName)
                if (app != null) {
                    // TODO 优化
                    val c = InAppInstListFragment.getInstList(it.packageName).size
                    tmp.add(ViewModel(app.name, icon = app.getIcon(GlobalApp.APP), subTitle = "数量: $c", extra = it.packageName))
                } else {//未安装 TODO app.info
//                    notInstalled.add(ViewModel(it.packageName, getString(R.string.text_not_installed), extra = it.packageName))
                }
                pkgSet.add(it.packageName)
            }
        }
//        tmp.addAll(notInstalled)
        return tmp
    }

    /**
     * 获取支持App列表
     */
    override fun onGetData(pageIndex: Int) {
        runOnPool {
            val list = DAO.daoSession.actionScopeDao
                    .queryBuilder()
                    .offset(pageSizeLimit * pageIndex)
                    .limit(pageSizeLimit)
                    .list()
            dataSet.addAll(transData(list))
            postLoadResult(list.isEmpty())
        }
    }
}
