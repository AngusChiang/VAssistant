package cn.vove7.jarvis.fragments

import android.view.View
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel

/**
 * # SimpleListFragment
 * > with a [SimpleListAdapter]
 * @author 17719247306
 * 2018/8/18
 */
abstract class SimpleListFragment<DataType> : VListFragment() {

    open val itemClickListener: SimpleListAdapter.OnItemClickListener? = null
    open val pageSizeLimit = 10

    val dataSet = mutableListOf<ViewModel>()
    override fun clearDataSet() {
        dataSet.clear()
    }

    override fun initView(contentView: View) {
        adapter = SimpleListAdapter(dataSet,itemClickListener)
    }

    /**
     * 转类型
     */
    abstract fun transData(nodes: List<DataType>): List<ViewModel>
}