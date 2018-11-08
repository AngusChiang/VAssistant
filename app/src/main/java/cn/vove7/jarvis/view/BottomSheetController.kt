package cn.vove7.jarvis.view

import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.Snackbar
import android.view.View
import android.view.ViewGroup
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.BottomListAdapter
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.vtp.view.listview.BottomSheetListView

/**
 * # BottomSheetController
 * with ListView
 *
 * @author 17719247306
 * 2018/8/19
 */
open class BottomSheetController(
        val context: Context, val bottomView: View
) {
    init {
        bottomView.visibility = View.VISIBLE
        initBottomSheetView()
    }


    var behavior: BottomSheetBehavior<*>? = null

    lateinit var bottomListView: BottomSheetListView
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

    /**
     * 复写onItemClick
     */
    open fun initBottomSheetView(hide: Boolean = true) {
        bottomListView = f(R.id.bottom_list_view)
        behavior = BottomSheetBehavior.from(bottomView)
        if (hide)
            hideBottom()
    }

    //设置BottomList数据
    fun setBottomListData(dataSet: MutableList<ViewModel>, iClickListener: SimpleListAdapter.OnItemClickListener) {
        listAdapter = BottomListAdapter(context, dataSet, iClickListener)
        bottomListView.adapter = listAdapter
        listAdapter.notifyDataSetChanged()
    }

    fun notifyDataSetChanged() {
        listAdapter.notifyDataSetChanged()
    }

    fun showSnack(parentView: ViewGroup, msg: String, hasAction: Boolean = false, listener: View.OnClickListener? = null) {
        val bar = Snackbar.make(parentView, msg, if (hasAction) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_SHORT)
        if (hasAction)
            bar.setAction("OK") { bar.dismiss() }
        if (listener != null) {
            bar.setAction("DO", listener)
        }
        bar.show()
    }


    fun <T : View> f(id: Int): T {
        return bottomView.findViewById(id)
    }

}
