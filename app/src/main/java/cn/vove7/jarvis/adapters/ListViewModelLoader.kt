package cn.vove7.jarvis.adapters

import android.view.MenuItem
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.utils.ThreadPool
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.fragments.AwesomeItem
import cn.vove7.jarvis.tools.SearchActionHelper
import cn.vove7.vtp.log.Vog

/**
 * # ViewModelLoader
 * 统一数据加载 封装  可被Activity Dialog Fragment 继承
 * @author Administrator
 * 2018/12/20
 */

interface ListViewModelLoader<DataType> {
    val pageSizeLimit: Int
    val dataSet: MutableList<ListViewModel<DataType>>

    /**
     * 是否 在一次性加载完数据时 排序
     */
    var sortData: Boolean
    var pageIndex: Int

    fun refreshList() {
        dataSet.clear()
        changeViewOnLoading()
        onLoadData(pageIndex)
    }

    fun onLoadData(pageIndex: Int)

    /**
     * 通知数据加载完成 ，
     * @param list List<DataType>
     * @param allLoad Boolean 是否加载全部，默认根据当前页数据量和pageSizeLimit比较
     */
    fun notifyLoadSuccess(list: Collection<DataType>, allLoad: Boolean = list.size < pageSizeLimit) {
        synchronized(dataSet) {
            dataSet.addAll(transData(list))
        }
        if (sortData && allLoad) dataSet.sort()
        Vog.d("notifyLoadSuccess ---> $allLoad")
        runOnUi {
            changeViewOnLoadDone(allLoad)
        }
        if (!allLoad) pageIndex++
    }

    /**
     * 加载完成改变视图
     * @param allLoad Boolean
     */
    fun changeViewOnLoadDone(allLoad: Boolean)

    /**
     * 加载动画
     */
    fun changeViewOnLoading() {}

    fun transData(nodes: Collection<DataType>): List<ListViewModel<DataType>> {
        val list = mutableListOf<ListViewModel<DataType>>()
        nodes.forEach {
            unification(it)?.also { d -> list.add(d) }
        }
        return list
    }

    //列表 统一化 数据格式 展示 DataType->ListViewModel
    fun unification(data: DataType): ListViewModel<DataType>? {
        if (data is AwesomeItem && data.isShow()) {
            return try {
                ListViewModel(data.title, data.subTitle, icon = data.viewIcon, checked = data.isChecked
                    ?: false, extra = data)
            } catch (e: Exception) {
                GlobalLog.err(e)
                null
            }
        } else throw RuntimeException("未继承AwesomeItem，并且未实现unification")
    }

    /**
     * 可查询
     * @param searchItem MenuItem
     */
    fun searchable(searchItem: MenuItem) {
        SearchActionHelper(searchItem) { text ->
            if (text == "") {
                refreshList()
                return@SearchActionHelper
            }

            changeViewOnLoading()
            ThreadPool.runOnCachePool {
                val tmp = dataSet.filter {
                    it.title?.contains(text, ignoreCase = true) == true ||
                            it.subTitle?.contains(text, ignoreCase = true) == true
                }
                dataSet.clear()
                dataSet.addAll(tmp)
                changeViewOnLoadDone(true)
            }
        }
    }
}