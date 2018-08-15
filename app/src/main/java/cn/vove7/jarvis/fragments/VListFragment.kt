package cn.vove7.jarvis.fragments

import android.os.Bundle
import android.os.Handler
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.RecAdapterWithFooter

/**
 * # VListFragment
 * 下拉刷新 上拉加载
 *
 * Created by Vove on 2018/8/13
 */
abstract class VListFragment : Fragment(), ListStatusListener {

    private lateinit var contentView: View
    private lateinit var netErrViewContainer: ViewGroup
    private var netErrView: View? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    var onRefreshing = false//下拉正在刷新标志

    var allLoadFlag = false//全部加载标志

    /**
     * 自由x`控制
     */
    var pageIndex = 0
    lateinit var recyclerView: RecyclerView

    var layManager: RecyclerView.LayoutManager? = null
    lateinit var adapter: RecAdapterWithFooter<*>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = inflater.inflate(R.layout.fragment_base_list, container, false)
        initSelfView()
        initView(contentView)
        afterSet()
        refresh()
        return contentView
    }

    private fun afterSet() {
        if (layManager != null)
            recyclerView.layoutManager = layManager
        else recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        netErrViewContainer.addView(
                if (netErrView == null) defaultErrView
                else netErrView
        )
    }

    abstract fun clearDataSet()

    @CallSuper
    override fun onGetData(pageIndex: Int) {
        if (pageIndex == 0) {
            clearDataSet()
            notifyDataSetChanged()
        }
    }

    private fun initSelfView() {
        recyclerView = f(R.id.recycle_view)

        swipeRefreshLayout = f(R.id.swipe_refresh)
        netErrViewContainer = f(R.id.net_error_layout)
        swipeRefreshLayout.setOnRefreshListener {
            //下拉刷新
            if (!onRefreshing) {
                refresh()
            }
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            //上拉加载
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!allLoadFlag && isSlideToBottom() && !onRefreshing) {//上拉加载,
                    Handler().postDelayed({
                        if (isSlideToBottom()) {
                            onRefreshing = true
                            loadMore()
                            if (pageIndex != 0)
                                adapter.setFooter(RecAdapterWithFooter.STATUS_LOADING)
                        }
                    }, 500)//延时
                }
            }
        })
    }

    /**
     * 全部加载
     */
    fun setAllLoad() {
        allLoadFlag = true
        adapter.setFooter(RecAdapterWithFooter.STATUS_ALL_LOAD)
    }

    fun showNetErr() {
        recyclerView.visibility = View.GONE
        netErrViewContainer.visibility = View.VISIBLE
    }

    fun showNetErrOnFotter() {
        adapter.setFooter(RecAdapterWithFooter.STATUS_NET_ERROR)
    }

    fun notifyDataSetChanged() {
        recyclerView.adapter.notifyDataSetChanged()
    }

    protected fun isSlideToBottom(): Boolean {
        return recyclerView.computeVerticalScrollExtent() +
                recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange()

    }

    fun refresh() {
        allLoadFlag = false
        swipeRefreshLayout.isRefreshing = true
        pageIndex = 0
        onGetData(0)
    }

    fun loadMore() {
        onGetData(pageIndex)
    }

    /**
     * 设置布局状态
     */
    fun setContentStatus() {

    }

    fun stopRefreshing() {
        onRefreshing = false
        if (swipeRefreshLayout.isRefreshing)
            swipeRefreshLayout.isRefreshing = false
    }

    /**
     * 内容布局显示
     * pageIndex++
     * stopRefreshing
     * notifyDataSetChanged
     * @param allLoad 全部加载标志
     */
    fun notifyLoadSuccess(allLoad: Boolean = false) {
        recyclerView.visibility = View.VISIBLE
        netErrViewContainer.visibility = View.GONE
        pageIndex++
        onRefreshing = false
        stopRefreshing()
        notifyDataSetChanged()
        if (allLoad) {
            setAllLoad()
        } else
            adapter.hideFooterView()
    }

    private val defaultErrView: View
        get() =
            LayoutInflater.from(context).inflate(R.layout.net_error_layout, null, false)

    abstract fun initView(contentView: View)
    private fun <V : View> f(id: Int): V = contentView.findViewById(id) as V

}

interface ListStatusListener {
    fun onGetData(pageIndex: Int)
}