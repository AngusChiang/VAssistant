package cn.vove7.jarvis.fragments

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.executor.entity.MarkedData.MARKED_TYPE_APP
import cn.vove7.common.datamanager.greendao.MarkedDataDao
import cn.vove7.common.utils.CoroutineExt.launch
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.fragments.base.BaseMarkedFragment
import cn.vove7.jarvis.view.dialog.SelectAppDialog

/**
 * # MarkedAppFragment
 *
 * @author 17719247306
 * 2018/9/7
 */
class MarkedAppFragment : BaseMarkedFragment() {

    private var showUninstall = false

    override var sortData: Boolean = true

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

    val d by lazy {
        SelectAppDialog.get(activity as BaseActivity<*>) {
            setValue(it.packageName)
            if (getKey() != "") {
                setKey(it.name ?: "")
            }
        }
    }

    override fun onSelect() {//app list
        d.show()
    }

    override fun transData(nodes: Collection<MarkedData>): List<ListViewModel<MarkedData>> {
        val ss = mutableListOf<ListViewModel<MarkedData>>()
        val sss = mutableListOf<ListViewModel<MarkedData>>()
        nodes.forEach {
            val app = SystemBridge.getAppInfo(it.value)
            if (app == null) {
                if (showUninstall)
                    sss.add(ListViewModel(it.key, it.value, null, it))
            } else ss.add(ListViewModel(it.key, app.name, app.icon, it))
        }
        ss.addAll(sss)// 显示在最后
        return ss
    }

    /**
     * 加载用户应用列表、系统应用，
     *
     * @param pageIndex Int
     */
    override fun onLoadData(pageIndex: Int) {
        launch {
            val builder = DAO.daoSession.markedDataDao
                    .queryBuilder()
                    .where(MarkedDataDao.Properties.Type.eq(MARKED_TYPE_APP))

            val list = builder.list()
            notifyLoadSuccess(list,true)
        }
    }
}