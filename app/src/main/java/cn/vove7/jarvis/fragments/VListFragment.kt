package cn.vove7.jarvis.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.annotation.LayoutRes
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.RecAdapterWithFooter
import cn.vove7.vtp.log.Vog
import com.l4digital.fastscroll.FastScrollRecyclerView
import kotlinx.android.synthetic.main.fragment_base_list.*

/**
 * # VListFragment
 * 下拉刷新 上拉加载
 *
 * Created by Vove on 2018/8/13
 */
abstract class VListFragment : Fragment() {

    private lateinit var contentView: View
    private lateinit var netErrViewContainer: ViewGroup
    private var netErrView: View? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout//TODO pullDown
    open val pageSizeLimit = 15

    //    var onRefreshing = false//下拉正在刷新标志
    var loading = false
    var allLoadFlag = false//全部加载标志

    lateinit var floatButton: FloatingActionButton
    open var floatClickListener: View.OnClickListener? = null

    /**
     * 自由x`控制
     */
    var pageIndex = 0
    lateinit var recyclerView: FastScrollRecyclerView

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

    //    var viewCreate = false
//    var firstLoad = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        float_header.visibility = View.GONE
//        viewCreate = true
    }

    /**
     * 懒加载
     * @param isVisibleToUser Boolean
     */
//    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
//        super.setUserVisibleHint(isVisibleToUser)
//        Vog.d(this,"setUserVisibleHint ---> ${this} $firstLoad")
//        if (viewCreate && isVisibleToUser && !firstLoad) {
//            firstLoad = true
//            Vog.d(this,"setUserVisibleHint ---> refresh ${this}")
//            refresh()
//        }
//    }

    fun setHeader(@LayoutRes layId: Int) {
        val v = layoutInflater.inflate(layId, null, false)
        setHeader(v)
    }

    fun addHeader(@LayoutRes layId: Int) {
        val v = layoutInflater.inflate(layId, null, false)
        addHeader(v)
    }

    fun setHeader(v: View) {
        float_header.removeAllViews()
        float_header.addView(v)
        float_header.visibility = View.VISIBLE
    }

    fun addHeader(v: View) {
        float_header.addView(v)
        float_header.visibility = View.VISIBLE
    }

    fun hideHeader() {
        float_header.visibility = View.GONE
    }

    private fun afterSet() {
        if (layManager != null && !layManager!!.isAttachedToWindow) {
            recyclerView.layoutManager = layManager
        } else recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = adapter
        netErrViewContainer.addView(
                if (netErrView == null) defaultErrView
                else netErrView
        )
        if (floatClickListener == null) {
            floatButton.visibility = GONE
        } else {
            floatButton.visibility = View.VISIBLE
            floatButton.setOnClickListener(floatClickListener)
        }
    }


    class ResultHandler(val f: VListFragment) : Handler() {
        override fun handleMessage(msg: Message?) {
            f.notifyLoadSuccess((msg?.what ?: 999) < f.pageSizeLimit)// all load
        }
    }

    val resultHandler = ResultHandler(this)
    abstract fun clearDataSet()

    abstract fun onGetData(pageIndex: Int)

    private fun initSelfView() {
        recyclerView = f(R.id.recycle_view)
        floatButton = f(R.id.fab)
        swipeRefreshLayout = f(R.id.swipe_refresh)
        netErrViewContainer = f(R.id.net_error_layout)
        swipeRefreshLayout.setOnRefreshListener {
            //下拉刷新
            refresh()
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            //上拉加载
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!allLoadFlag && isSlideToBottom() && !swipeRefreshLayout.isRefreshing) {//上拉加载,
                    if (pageIndex != 0)
                        adapter.setFooter(RecAdapterWithFooter.STATUS_LOADING)
                    Handler().postDelayed({
                        if (isSlideToBottom()) {
                            loadMore()
                        }
                    }, 800)//延时
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

    fun showNetErrOnFooter() {
        adapter.setFooter(RecAdapterWithFooter.STATUS_NET_ERROR)
    }

    fun notifyDataSetChanged() {
        recyclerView.adapter.notifyDataSetChanged()
    }

    protected fun isSlideToBottom(): Boolean {

        return recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >=
                recyclerView.computeVerticalScrollRange()

    }

    fun refresh() {
        if (loading) {
            Vog.d(this, "loading ")
            swipeRefreshLayout.isRefreshing = false
            return
        }
        swipeRefreshLayout.isRefreshing = true
        Vog.d(this, "refresh ")
        allLoadFlag = false
        adapter.animationsLocked = false
        pageIndex = 0
        adapter.hideFooterView()
        if (pageIndex == 0) {
            clearDataSet()
            notifyDataSetChanged()
        }
        onGetData(0)
    }

    fun loadMore() {
        Vog.d(this, "loadMore $pageIndex")
        if (swipeRefreshLayout.isRefreshing) return
        loading = true
        Vog.d(this, "onGetData $pageIndex")
        onGetData(pageIndex)
    }

    /**
     * 设置布局状态
     */
    fun setContentStatus() {//list - empty data - err

    }

    fun stopRefreshing() {
        Handler().postDelayed({
            swipeRefreshLayout.isRefreshing = false
        }, 500)
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
        loading = false
        stopRefreshing()
        notifyDataSetChanged()
        if (allLoad) {
            setAllLoad()
        } else
            adapter.hideFooterView()
    }

    fun buildHeader(title: String, switchChecked: Boolean = false, lis: CompoundButton.OnCheckedChangeListener? = null) {
        val v = layoutInflater.inflate(R.layout.list_header_with_switch, null, false)
        val headerTitle = v.findViewById<TextView>(R.id.header_title)
        val headerSwitch = v.findViewById<Switch>(R.id.header_switch)
        headerTitle.text = title
        headerSwitch.isChecked = switchChecked
        headerSwitch.setOnCheckedChangeListener(lis)
        v.setOnClickListener { headerSwitch.toggle() }
        setHeader(v)
    }

    private val defaultErrView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.net_error_layout, null, false)

    abstract fun initView(contentView: View)
    private fun <V : View> f(id: Int): V = contentView.findViewById(id) as V

}
