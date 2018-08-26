package cn.vove7.jarvis.view

import android.content.Context
import android.support.annotation.MenuRes
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.BottomListAdapter
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.vtp.view.listview.BottomSheetListView

/**
 * # BottomSheetController
 * ListView
 *
 * @author 17719247306
 * 2018/8/19
 */
class BottomSheetController(
        val context: Context, val bottomView: View, var bottomTitle: String? = null, @MenuRes val menuId: Int
) {
    init {
        bottomView.visibility = View.VISIBLE
        initBottomSheetView(menuId)
    }


    fun setTitle(title: String?) {
        if (title != null) {
            this.bottomTitle = title
            bottomToolbar.title = title
        }
    }

    var behavior: BottomSheetBehavior<*>? = null

    lateinit var bottomToolbar: Toolbar
    lateinit var bottomListView: BottomSheetListView
    lateinit var errLayout: View
    lateinit var errText: TextView
    lateinit var progressBar: View
    lateinit var listAdapter: BottomListAdapter

    val isBottomSheetShowing: Boolean
        get() = behavior?.state != BottomSheetBehavior.STATE_HIDDEN


    fun hideBottom() {
        behavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun showBottom() {
        behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }
    fun expandSheet() {
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    companion object {
        const val SHOW_LIST = 0
        const val SHOW_PROCESS = 1
        const val SHOW_ERR = 2
    }

    /**
     * 复写onItemClick
     */
    fun initBottomSheetView(menuId: Int? = null) {
        bottomListView = f(R.id.bottom_list_view)
//        bottomListView.layoutManager = LinearLayoutManager(context)
        bottomToolbar = f(R.id.bottom_toolbar)
        progressBar = f(R.id.refresh_bar)
        errLayout = f(R.id.err_layout)
        bottomToolbar.setNavigationOnClickListener { _ -> hideBottom() }
//        val bottomView = f<View>(R.id.bottom_sheet)
        behavior = BottomSheetBehavior.from(bottomView)
        hideBottom()
        if (menuId !== null)
            bottomToolbar.inflateMenu(menuId)

        setTitle(bottomTitle)
    }

    //设置BottomList数据
    fun setBottomListData(dataset: MutableList<ViewModel>, iClickListener: SimpleListAdapter.OnItemClickListener) {
        listAdapter = BottomListAdapter(context, dataset, iClickListener)
        bottomListView.adapter = listAdapter
        listAdapter.notifyDataSetChanged()
    }

    fun notifyDataSetChanged() {
        listAdapter.notifyDataSetChanged()
    }

    fun onSuccess() {
        changeBottomLayout(SHOW_LIST)
    }

    fun onLoading() {
        changeBottomLayout(SHOW_PROCESS)
    }

    fun onErr(msg: String? = null) {
        if (msg != null)
            errText.text = msg
        changeBottomLayout(SHOW_ERR)
    }

    //改变bottom布局，刷新，err
    fun changeBottomLayout(showIndex: Int) {
        val views = arrayOf(bottomListView, progressBar, errLayout)

        for ((index, v) in views.withIndex()) {
            if (showIndex == index)
                v.visibility = View.VISIBLE
            else
                v.visibility = View.GONE
        }
    }


    fun showSnack(parentView: ViewGroup, msg: String, hasAction: Boolean = false, listener: View.OnClickListener? = null) {
        val bar = Snackbar.make(parentView, msg, if (hasAction) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_SHORT)
        if (hasAction)
            bar.setAction("OK") { _ -> bar.dismiss() }
        if (listener != null) {
            bar.setAction("DO", listener)
        }
        bar.show()
    }


    fun <T : View> f(id: Int): T {
        return bottomView.findViewById(id)
    }

}
