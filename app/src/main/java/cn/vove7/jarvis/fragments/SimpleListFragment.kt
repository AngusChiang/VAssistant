package cn.vove7.jarvis.fragments

import android.graphics.drawable.Drawable
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.view.RecyclerViewWithContextMenu
import cn.vove7.vtp.log.Vog

/**
 * # SimpleListFragment
 * Model 统一显示
 * > with a [SimpleListAdapter] [ViewModel]
 * @author 17719247306
 * 2018/8/18
 */
abstract class SimpleListFragment<DataType> : VListFragment() {

    open val itemClickListener: SimpleListAdapter.OnItemClickListener<DataType>? = null

    val dataSet = mutableListOf<ViewModel<DataType>>()
    override fun clearDataSet() {
        synchronized(dataSet) {
            dataSet.clear()
        }
    }

    override fun initView(contentView: View) {
        adapter = SimpleListAdapter(dataSet, itemClickListener, itemCheckable)
    }

    /**
     * 转类型
     */
    open fun transData(nodes: List<DataType>): List<ViewModel<DataType>> {
        val list = mutableListOf<ViewModel<DataType>>()
        nodes.forEach {
            unification(it)?.also { d -> list.add(d) }
        }
        return list
    }

    /**
     * 统一化  DataType -> ViewModel
     *
     * DataType 可实现接口AwesomeItem 自动
     * @param data DataType
     * @return ViewModel
     */
    open fun unification(data: DataType): ViewModel<DataType>? {
        if (data is AwesomeItem && data.isShow()) {
            return try {
                ViewModel(data.title, data.subTitle, icon = data.viewIcon, checked = data.isChecked
                    ?: false, extra = data)
            } catch (e: Exception) {
                GlobalLog.err(e, "uni54")
                null
            }
        }
        return null
    }

    /**
     * 通知数据加载完成 ，
     * @param list List<DataType>
     * @param allLoad Boolean 是否加载全部，默认根据当前页数据量和pageSizeLimit比较
     */
    fun notifyLoadSuccess(list: List<DataType>, allLoad: Boolean = list.size < pageSizeLimit) {
        synchronized(dataSet) {
            dataSet.addAll(transData(list))
        }
        Vog.d(this, "notifyLoadSuccess ---> $allLoad")
        notifyLoadSuccess(allLoad)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        val p = (item?.menuInfo as RecyclerViewWithContextMenu
        .RecyclerViewContextInfo?)?.position ?: -1
        return if (p in 0 until dataSet.size)
            onItemPopupMenu(item, p, dataSet[p])
        else false
    }


    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        val p = (menuInfo as RecyclerViewWithContextMenu
        .RecyclerViewContextInfo?)?.position ?: -1
        if (p in 0 until dataSet.size && menu != null)
            onCreatePopupMenu(menu, p, dataSet[p])
    }

    /**
     * 构建item弹出菜单
     * @param menu ContextMenu?
     * @param pos Int
     * @param viewItem ViewModel<DataType>
     */
    open fun onCreatePopupMenu(menu: ContextMenu, pos: Int, viewItem: ViewModel<DataType>) {}


    /**
     * item长按弹出菜单
     * @param item MenuItem?
     * @param pos Int
     * @param viewItem ViewModel<DataType>?
     * @return Boolean
     */
    open fun onItemPopupMenu(item: MenuItem?, pos: Int, viewItem: ViewModel<DataType>): Boolean = false

}

/**
 * 统一显示
 * @property title String?
 * @property subTitle String?
 * @property bgColor Int?
 * @property viewModel ViewModel<*>?
 */
interface AwesomeItem {
    val title: String?
    fun isShow(code: Int? = null): Boolean = true
    val subTitle: String?
    val viewIcon: Drawable? get() = null
    val isChecked: Boolean? get() = null
    val bgColor: Int? get() = null
}