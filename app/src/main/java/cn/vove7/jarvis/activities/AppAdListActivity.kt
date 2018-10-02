package cn.vove7.jarvis.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import cn.vove7.common.datamanager.AppAdInfo
import cn.vove7.common.datamanager.DAO
import cn.vove7.common.datamanager.greendao.AppAdInfoDao
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.utils.NetHelper
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.OneFragmentActivity
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.fragments.SimpleListFragment
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.jarvis.utils.DialogUtil
import cn.vove7.jarvis.view.dialog.AdEditorDialog
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog

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
                intent.getStringExtra("pkg"))
        fragments = arrayOf(f)
    }

    /**
     * # AppAdListFragment
     * @property list ArrayList<AppAdInfo>?
     */
    class AppAdListFragment : SimpleListFragment<AppAdInfo>() {
        lateinit var pkg: String

        val editDialog: AdEditorDialog by lazy {
            AdEditorDialog(context!!) {
                refresh()
            }
        }

        override val itemClickListener: SimpleListAdapter.OnItemClickListener? =
            object : SimpleListAdapter.OnItemClickListener {
            @SuppressLint("CheckResult")
            override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {

                val data = item.extra as AppAdInfo
                MaterialDialog(context!!).show {
                    if (data.belongUser(true)) {
                        neutralButton(R.string.text_edit) {
                            editDialog.show(item.extra)
                        }
                        positiveButton(R.string.text_share) {
                            if (!AppConfig.checkUser()) {
                                return@positiveButton
                            }
                            share(data)
                        }
                        negativeButton(R.string.text_delete) {
                            val tag = data.tagId
                            DialogUtil.dataDelAlert(context) {
                                if (tag != null) {
                                    delRemoteShare(tag)
                                }
                                DAO.daoSession.appAdInfoDao.delete(data)
                                toast.showShort(R.string.text_delete_complete)
                                refresh()
                            }
                        }
                    }
                    title(text = item.title)
                    message(text = data.toString())
                }
            }
        }
        override var floatClickListener: View.OnClickListener? = View.OnClickListener {
            if (!AppConfig.checkUser()) {
                return@OnClickListener
            }
            editDialog.show()
        }

        companion object {
            fun newInstance(pkg: String): AppAdListFragment {
                return AppAdListFragment().also {
                    it.pkg = pkg
                }
            }
        }

        fun share(adInfo: AppAdInfo) {
            NetHelper.postJson<String>(ApiUrls.SHARE_APP_AD_INFO, BaseRequestModel(adInfo),
                    type = NetHelper.StringType) { _, bean ->
                if (bean != null) {
                    if (bean.isOk()) {
                        //return tagId
                        val tag = bean.data
                        if (tag != null) {
                            adInfo.from = DataFrom.FROM_SHARED
                            adInfo.tagId = tag
                            DAO.daoSession.appAdInfoDao.update(adInfo)
                        }
                        toast.green().showLong(bean.message)
                    } else {
                        toast.showLong(bean.message)
                    }
                } else
                    toast.red().showShort(R.string.text_error_occurred)
            }
        }

        fun delRemoteShare(tag: String) {
            NetHelper.postJson<Any>(ApiUrls.DELETE_SHARE_APP_AD, BaseRequestModel(tag)) { _, bean ->
                if (bean?.isOk() == true) {
                    Vog.d(this, "deleteShare ---> 云端删除成功")
                } else
                    Vog.d(this, "deleteShare ---> 云端删除失败")
            }
        }

        override fun transData(nodes: List<AppAdInfo>): List<ViewModel> {
            val list = mutableListOf<ViewModel>()
            nodes.forEach {
                list.add(ViewModel(it.descTitle, it.activity, extra = it))
            }
            return list
        }

        override fun onGetData(pageIndex: Int) {
            val list = DAO.daoSession.appAdInfoDao
                    .queryBuilder()
                    .where(AppAdInfoDao.Properties.Pkg.eq(pkg)).list()

            dataSet.addAll(transData(list))
            notifyLoadSuccess(true)
        }
    }

}