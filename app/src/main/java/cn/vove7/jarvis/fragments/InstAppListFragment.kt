package cn.vove7.jarvis.fragments

import android.content.Intent
import android.view.View
import cn.vove7.android.common.logi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.datamanager.parse.statusmap.ActionNode.NODE_SCOPE_IN_APP
import cn.vove7.common.helper.AdvanAppHelper.ALL_APP_LIST
import cn.vove7.common.model.UserInfo
import cn.vove7.jarvis.activities.InAppInstActivity
import cn.vove7.jarvis.activities.NewInstActivity
import cn.vove7.jarvis.activities.OnSyncInst
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.tools.DataUpdator
import cn.vove7.vtp.app.AppHelper
import cn.vove7.vtp.log.Vog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * # InstAppListFragment
 * App支持列表
 * @author Vove
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

    override val itemClickListener: SimpleListAdapter.OnItemClickListener<ActionScope> = object : SimpleListAdapter.OnItemClickListener<ActionScope> {
        override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ListViewModel<ActionScope>) {
            val intent = Intent(context, InAppInstActivity::class.java)
            intent.putExtra("pkg", item.extra.packageName)
            intent.putExtra("title", item.title)
            startActivity(intent)
        }
    }

    override fun onSync() {
        if (!UserInfo.isLogin()) {
            GlobalApp.toastWarning("请登陆后操作")
            return
        }
        showProgressBar()
        launchIO {
            if (DataUpdator.syncInAppInst()) {
                GlobalApp.toastSuccess("同步完成")
                withContext(Dispatchers.Main) {
                    refresh()
                }
            }
            withContext(Dispatchers.Main) {
                hideProgressBar()
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

    override fun unification(it: ActionScope): ListViewModel<ActionScope>? {
        if (it.packageName == null || pkgSet.contains(it.packageName))
            return null
        val app = ALL_APP_LIST[it.packageName]
        val node = if (app != null) {
            // TODO 优化
            val c = InAppInstListFragment.getInstList(it.packageName).size
            ListViewModel(app.name, icon = app.icon,
                    subTitle = "数量: $c", extra = it)
        } else {//未安装 TODO app.info
//                    notInstalled.add(ListViewModel(it.packageName, getString(R.string.text_not_installed), extra = it.packageName))
            null
        }
        pkgSet.add(it.packageName)
        return node
    }

    /**
     * 获取支持App列表
     */
    override fun onLoadData(pageIndex: Int) {
        launch {
            val list = DAO.daoSession.actionScopeDao
                    .queryBuilder()
                    .offset(pageSizeLimit * pageIndex)
                    .limit(pageSizeLimit)
                    .list()
            notifyLoadSuccess(list)
        }
    }
}
