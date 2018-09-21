package cn.vove7.jarvis.activities

import android.os.Bundle
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.datamanager.AppAdInfo
import cn.vove7.common.datamanager.parse.DataFrom
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.OneFragmentActivity
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.fragments.SimpleListFragment
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
                intent.getSerializableExtra("list") as ArrayList<AppAdInfo>)
        fragments = arrayOf(f)
    }

    /**
     * # AppAdListFragment
     * @property list ArrayList<AppAdInfo>?
     */
    class AppAdListFragment : SimpleListFragment<AppAdInfo>() {
        override val itemClickListener: SimpleListAdapter.OnItemClickListener? = object : SimpleListAdapter.OnItemClickListener {
            override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
                // TODO detail
                val data = item.extra as AppAdInfo?
                MaterialDialog(context!!).show {
                    if (DataFrom.userCanEdit(data?.from)) {
                        neutralButton(R.string.text_edit) {
                            //todo edit
                            onEdit(item.extra as AppAdInfo)
                        }
                    }
                    title(text = item.title)
                    message(text = data.toString())
                }
            }
        }

        fun onEdit(data: AppAdInfo) {
        }

        companion object {
            fun newInstance(list: ArrayList<AppAdInfo>): AppAdListFragment {
                return AppAdListFragment().also {
                    it.list = list
                }
            }
        }

        var list: ArrayList<AppAdInfo>? = null

        override fun transData(nodes: List<AppAdInfo>): List<ViewModel> {
            val list = mutableListOf<ViewModel>()
            nodes.forEach {
                list.add(ViewModel(it.descTitle, it.activity, extra = it))
            }
            return list
        }

        override fun onGetData(pageIndex: Int) {
            if (list != null)
                dataSet.addAll(transData(list!!.toList()))
            else GlobalApp.toastShort(getString(R.string.text_error_occurred))
            notifyLoadSuccess(true)
        }
    }

}