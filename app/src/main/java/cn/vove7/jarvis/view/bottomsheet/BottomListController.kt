package cn.vove7.jarvis.view.bottomsheet

import android.content.Context
import android.view.View
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.BottomListAdapter
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.vtp.view.listview.BottomSheetListView

/**
 * # BottomListController
 * with ListView
 *
 * @author 17719247306
 * 2018/8/19
 */
open class BottomListController<Type>(
        context: Context, bottomView: View
) : BottomSheetController(context, bottomView){

    lateinit var bottomListView: BottomSheetListView
    lateinit var listAdapter: BottomListAdapter<Type>


    /**
     * 复写onItemClick
     */
    override fun initBottomSheetView() {
        bottomListView = f(R.id.bottom_list_view)
    }

    //设置BottomList数据
    fun setBottomListData(dataSet: List<ListViewModel<Type>>, iClickListener: SimpleListAdapter.OnItemClickListener<Type>) {
        listAdapter = BottomListAdapter(context, dataSet, iClickListener)
        bottomListView.adapter = listAdapter
        listAdapter.notifyDataSetChanged()
    }

    fun notifyDataSetChanged() {
        listAdapter.notifyDataSetChanged()
    }

}
