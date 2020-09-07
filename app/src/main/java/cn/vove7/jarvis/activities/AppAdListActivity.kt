package cn.vove7.jarvis.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.buttons
import cn.vove7.bottomdialog.builder.message
import cn.vove7.bottomdialog.builder.title
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.AppAdInfo
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.AppAdInfoDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.activities.base.OneFragmentActivity
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.app.AppApi
import cn.vove7.jarvis.fragments.SimpleListFragment
import cn.vove7.jarvis.tools.DialogUtil
import cn.vove7.jarvis.view.dialog.AdEditorDialog
import cn.vove7.jarvis.view.positiveButtonWithColor
import cn.vove7.vtp.log.Vog

/**
 * # AppAdListActivity
 *
 * @author 17719247306
 * 2018/9/7
 */
class AppAdListActivity : OneFragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra("title")
    }

    override fun beforeSetViewPager() {
        val f = AppAdListFragment.newInstance(
                intent.getStringExtra("pkg")!!)
        fragments = arrayOf(f)
    }

    /**
     * # AppAdListFragment
     * @property list ArrayList<AppAdInfo>?
     */
    class AppAdListFragment : SimpleListFragment<AppAdInfo>() {
        lateinit var pkg: String

        val editDialog: AdEditorDialog by lazy {
            AdEditorDialog(activity as BaseActivity) {
                refresh()
            }
        }

        override val itemClickListener: SimpleListAdapter.OnItemClickListener<AppAdInfo>? =
            object : SimpleListAdapter.OnItemClickListener<AppAdInfo> {
                @SuppressLint("CheckResult")
                override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ListViewModel<AppAdInfo>) {
                    val data = item.extra

                    BottomDialog.builder(activity!!) {
                        title(item.title ?: "", true)
                        message(data.toString())
                        expandable = false
                        peekHeightProportion = 0.6f
                        if (data.belongUser()) {
                            buttons {
                                neutralButton(getString(R.string.text_edit)) {

                                    editDialog.show(item.extra)
                                }
                                positiveButtonWithColor(getString(R.string.text_share)) {
                                    if (!AppConfig.checkLogin()) {
                                        return@positiveButtonWithColor
                                    }
                                    share(data)
                                }
                                negativeButton(getString(R.string.text_delete)) {
                                    val tag = data.tagId
                                    DialogUtil.dataDelAlert(context) {
                                        if (tag != null) {
                                            delRemoteShare(tag)
                                        }
                                        DAO.daoSession.appAdInfoDao.delete(data)
                                        GlobalApp.toastSuccess(R.string.text_delete_complete)
                                        refresh()
                                    }
                                }
                            }
                        }
                    }
                }
            }

        override var floatClickListener: View.OnClickListener? = View.OnClickListener {
            if (!AppConfig.checkLogin()) {
                return@OnClickListener
            }
            editDialog.show(null, pkg)
        }

        companion object {
            fun newInstance(pkg: String): AppAdListFragment {
                Vog.d(pkg)
                return AppAdListFragment().also {
                    it.pkg = pkg
                }
            }
        }

        fun share(adInfo: AppAdInfo) = launchIO {
            kotlin.runCatching {
                AppApi.shareAppAdInfo(adInfo)
            }.onSuccess { bean ->
                if (bean.isOk()) {
                    //return tagId
                    val tag = bean.data
                    if (tag != null) {
                        adInfo.from = DataFrom.FROM_SHARED
                        adInfo.tagId = tag
                        DAO.daoSession.appAdInfoDao.update(adInfo)
                    }
                    GlobalApp.toastSuccess(bean.message)
                } else {
                    GlobalApp.toastInfo(bean.message)
                }
            }.onFailure { e ->
                GlobalLog.err(e)
                GlobalApp.toastError(R.string.text_error_occurred)
            }
        }

        fun delRemoteShare(tag: String) = launchIO {
            kotlin.runCatching {
                AppApi.deleteAdAppInfo(tag)
            }.onSuccess { bean ->
                if (bean.isOk()) {
                    Vog.d("云端删除成功")
                } else
                    Vog.d("云端删除失败")
            }.onFailure { e ->
                GlobalLog.err(e)
                Vog.d("云端删除失败")
            }
        }

        override fun unification(it: AppAdInfo): ListViewModel<AppAdInfo>? {
            return ListViewModel(it.descTitle, it.activity, extra = it)
        }

        override fun onLoadData(pageIndex: Int) {
            val list = DAO.daoSession.appAdInfoDao
                    .queryBuilder()
                    .where(AppAdInfoDao.Properties.Pkg.eq(pkg)).list()
            notifyLoadSuccess(list)
        }
    }

}