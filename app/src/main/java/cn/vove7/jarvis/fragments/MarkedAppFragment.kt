package cn.vove7.jarvis.fragments

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.executor.entity.MarkedData.MARKED_TYPE_APP
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.utils.ThreadPool.runOnCachePool
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.fragments.base.BaseMarkedFragment
import cn.vove7.jarvis.tools.DialogUtil

/**
 * # MarkedAppFragment
 *
 * @author 17719247306
 * 2018/9/7
 */
class MarkedAppFragment : BaseMarkedFragment<MarkedData>() {

    var showUninstall = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildHeader("显示未安装应用", lis = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            showUninstall = isChecked
            refresh()
        })
    }

    override var markedType: String = MarkedData.MARKED_TYPE_APP
    override val keyHint: Int = R.string.text_show_name
    override val valueHint: Int = R.string.text_package_name
    override val lastKeyId: Int = R.string.key_last_sync_marked_app_date

    override fun onSelect() {//app list
        DialogUtil.showSelApp(context!!) {
            setValue(it.second)
            if (getKey() != "") {
                setKey(it.first)
            }
        }
    }

    override fun transData(nodes: List<MarkedData>): List<ViewModel> {
        val ss = mutableListOf<ViewModel>()
        val sss = mutableListOf<ViewModel>()
        nodes.forEach {
            val app = SystemBridge.getAppInfo(it.value)
            if (app == null) {
                if (showUninstall)
                    sss.add(ViewModel(it.key, it.value, null, it))
            } else ss.add(ViewModel(it.key, app.name, app.getIcon(GlobalApp.APP), it))
        }
        ss.addAll(sss)
        return ss
    }

    override fun onGetData(pageIndex: Int) {
        runOnCachePool {
            val builder = DAO.daoSession.markedDataDao
                    .queryBuilder()
                    .where(MarkedDataDao.Properties.Type.eq(MARKED_TYPE_APP))
                    .offset(pageSizeLimit * pageIndex)
                    .limit(pageSizeLimit)

            val list = builder.list()

            dataSet.addAll(transData(list))
            resultHandler.sendEmptyMessage(list.size)
        }
    }
}