package cn.vove7.jarvis.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.annotation.LayoutRes
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.*
import cn.vove7.common.interfaces.Searchable
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.RecAdapterWithFooter
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ListViewModelLoader
import cn.vove7.jarvis.view.RecyclerViewWithContextMenu
import cn.vove7.vtp.log.Vog
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller

/**
 * # SimpleListFragment
 * Model 统一显示
 * > with a [SimpleListAdapter] [ListViewModel]
 * @author 17719247306
 * 2018/8/18
 */
abstract class SimpleListFragment<DataType> : Fragment(), ListViewModelLoader<DataType> {
    override var sortData: Boolean = false
    open val itemClickListener: SimpleListAdapter.OnItemClickListener<DataType>? = null
    override val pageSizeLimit: Int = 50
    override val dataSet: MutableList<ListViewModel<DataType>> = mutableListOf()

    open fun clearDataSet() {
        synchronized(dataSet) {
            dataSet.clear()
        }
    }

    fun search(text: String) {
        when {
            dataSet.isEmpty() -> return
            text.trim().isEmpty() -> refresh()
            else -> onSearch(text)
        }
    }

    private fun onSearch(text: String) {
        val l = dataSet.filter {
            (it.extra is Searchable && it.extra.onSearch(text))
        }
        clearDataSet()
        dataSet.addAll(l)
        changeViewOnLoadDone(true)
    }

    open fun initView(contentView: View) {
        adapter = SimpleListAdapter(dataSet, itemClickListener, itemCheckable)
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
     * @param viewItem ListViewModel<DataType>
     */
    open fun onCreatePopupMenu(menu: ContextMenu, pos: Int, viewItem: ListViewModel<DataType>) {}


    /**
     * item长按弹出菜单
     * @param item MenuItem?
     * @param pos Int
     * @param viewItem ListViewModel<DataType>?
     * @return Boolean
     */
    open fun onItemPopupMenu(item: MenuItem?, pos: Int, viewItem: ListViewModel<DataType>): Boolean = false

    private lateinit var contentView: View
    lateinit var progressBar: ProgressBar
    protected lateinit var netErrViewContainer: ViewGroup
    private var netErrView: View? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    open val itemCheckable: Boolean = false

    //    var onRefreshing = false//下拉正在刷新标志
    var loading = false
    var allLoadFlag = false//全部加载标志

    lateinit var floatButton: FloatingActionButton
    open var floatClickListener: View.OnClickListener? = null

    /**
     * 页码
     */
    override var pageIndex = 0
    lateinit var recyclerView: RecyclerView
    lateinit var fastScroller: RecyclerFastScroller
    var layManager: RecyclerView.LayoutManager? = null
    lateinit var adapter: RecAdapterWithFooter<*, *>
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
//        float_header.visibility = View.GONE
//        viewCreate = true
    }

    /**
     * 懒加载
     * @param isVisibleToUser Boolean
     */
//    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
//        super.setUserVisibleHint(isVisibleToUser)
//        Vog.d("setUserVisibleHint ---> ${this} $firstLoad")
//        if (viewCreate && isVisibleToUser && !firstLoad) {
//            firstLoad = true
//            Vog.d("setUserVisibleHint ---> refresh ${this}")
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

    val float_header by lazy { contentView.findViewById<CardView>(R.id.float_header) }
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
            floatButton.hide()
        } else {
            floatButton.show()
            floatButton.setOnClickListener(floatClickListener)
        }
    }

    private fun initSelfView() {
        recyclerView = f(R.id.recycle_view)
        fastScroller = f(R.id.fast_scroller)
        fastScroller.attachRecyclerView(recyclerView)
        floatButton = f(R.id.fab)
        swipeRefreshLayout = f(R.id.swipe_refresh)
        netErrViewContainer = f(R.id.net_error_layout)
        progressBar = f(R.id.progress_bar)
        progressBar.visibility = showBar
        swipeRefreshLayout.setOnRefreshListener {
            //下拉刷新
            refresh()
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            //上拉加载
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
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
        recyclerView.adapter?.notifyDataSetChanged()
    }

    protected fun isSlideToBottom(): Boolean {

        return recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >=
                recyclerView.computeVerticalScrollRange()

    }

    fun refresh() {
        if (loading) {
            Vog.d("loading ")
            swipeRefreshLayout.isRefreshing = false
            return
        }
        swipeRefreshLayout.isRefreshing = true
        Vog.d("refresh ")
        allLoadFlag = false
        adapter.animationsLocked = false
        pageIndex = 0
        adapter.hideFooterView()
        if (pageIndex == 0) {
            clearDataSet()
            notifyDataSetChanged()
        }
        onLoadData(0)
    }

    private fun loadMore() {
        if (loading) return
        loading = true
        Vog.d("pageIndex $pageIndex")
        if (swipeRefreshLayout.isRefreshing) return
        onLoadData(pageIndex)
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

    fun startRefreshing() {
        swipeRefreshLayout.isRefreshing = true
    }

    /**
     * 内容布局显示
     * pageIndex++
     * stopRefreshing
     * notifyDataSetChanged
     * @param allLoad 全部加载标志
     */
    override fun changeViewOnLoadDone(allLoad: Boolean) {
        runOnUi {
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
    }

    fun failedToLoad() {
        stopRefreshing()
        showNetErr()
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

    private fun <V : View> f(id: Int): V = contentView.findViewById(id) as V

    fun showProgressBar() {
        showBar = View.VISIBLE
    }

    private var showBar = View.GONE
        set(v) {
            try {
                progressBar.visibility = v
            } catch (e: Exception) {
            }
            field = v
        }

    fun hideProgressBar() {
        showBar = View.GONE
    }
}

/**
 * 统一显示
 * @property title String?
 * @property subTitle String?
 * @property bgColor Int?
 * @property viewModel ListViewModel<*>?
 */
interface AwesomeItem {
    val title: String?
    fun isShow(code: Int? = null): Boolean = true
    val subTitle: String?
    val viewIcon: Drawable? get() = null
    val isChecked: Boolean? get() = null
    val bgColor: Int? get() = null
    fun onLoadDrawable(imgView: ImageView) {}
}