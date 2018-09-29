package cn.vove7.jarvis.fragments

import android.os.Handler
import android.view.View
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel

/**
 * # SimpleListFragment
 * > with a [SimpleListAdapter] [ViewModel]
 * @author 17719247306
 * 2018/8/18
 */
abstract class SimpleListFragment<DataType> : VListFragment() {

    open val itemClickListener: SimpleListAdapter.OnItemClickListener? = null


    val dataSet = mutableListOf<ViewModel>()
    override fun clearDataSet() {
        dataSet.clear()
    }

    override fun initView(contentView: View) {
        adapter = SimpleListAdapter(dataSet, itemClickListener)
    }

    /**
     * 转类型
     */
    abstract fun transData(nodes: List<DataType>): List<ViewModel>

    fun postLoadResult(allLoad: Boolean) {
        handler.sendEmptyMessage(if (allLoad) 1 else 0)
    }

    val handler = Handler {
        notifyLoadSuccess(it.what == 1)
        return@Handler true
    }
}