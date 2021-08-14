package cn.vove7.jarvis.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.vove7.common.interfaces.Searchable
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.ListViewModel
import cn.vove7.jarvis.adapters.ListViewModelLoader
import cn.vove7.jarvis.adapters.RecAdapterWithFooter
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.databinding.FragmentBaseListBinding
import cn.vove7.jarvis.databinding.ListHeaderWithSwitchBinding
import cn.vove7.jarvis.lifecycle.LifeCycleScopeDelegate
import cn.vove7.jarvis.lifecycle.LifecycleScope
import cn.vove7.vtp.log.Vog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import java.util.stream.Collectors

/**
 * # SimpleListFragment
 * Model 统一显示
 * > with a [SimpleListAdapter] [ListViewModel]
 * @author 17719247306
 * 2018/8/18
 */
abstract class SimpleListFragment<DataType> :
        Fragment(), LifeCycleScopeDelegate, ListViewModelLoader<DataType> {
    override var sortData: Boolean = false
    open val itemClickListener: SimpleListAdapter.OnItemClickListener<DataType>? = null
    override val pageSizeLimit: Int = 50
    override val dataSet:  MutableList<ListViewModel<DataType>> = mutableListOf()

    override val lifecycleScope by lazy {
        LifecycleScope(lifecycle)
    }

    var refreshable: Boolean = true
        set(value) {
            field = value
            if (::contentView.isInitialized) {
                contentView.swipeRefresh.isEnabled = value
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

    open fun initView(contentView: FragmentBaseListBinding) {
        adapter = SimpleListAdapter(dataSet, itemClickListener, itemCheckable)
        refreshable = refreshable
    }

    private lateinit var contentView: FragmentBaseListBinding
    lateinit var progressBar: ProgressBar
    protected lateinit var netErrViewContainer: ViewGroup
    private var netErrView: View? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    open val itemCheckable: Boolean = false

    //    var onRefreshing = false//下拉正在刷新标志
    var loading = false
    var allLoadFlag = false//全部加载标志

    lateinit var floatButton: FloatingActionButton

    //null则隐藏按钮
    open var floatClickListener: View.OnClickListener? = null

    /**
     * 页码
     */
    override var pageIndex = 0
    lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    lateinit var fastScroller: RecyclerFastScroller
    var layManager: androidx.recyclerview.widget.RecyclerView.LayoutManager? = null
    lateinit var adapter: RecAdapterWithFooter<*, *>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = FragmentBaseListBinding.inflate(inflater,container,false)
        initSelfView()
        initView(contentView)
        afterSet()
        refresh()
        return contentView.root
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

    val float_header by lazy { contentView.floatHeader }

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
        recyclerView.layoutManager = if (layManager?.isAttachedToWindow == false) layManager
        else LinearLayoutManager(context)

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
        recyclerView = contentView.recycleView
        fastScroller = contentView.fastScroller
        fastScroller.attachRecyclerView(recyclerView)
        floatButton = contentView.fab
        swipeRefreshLayout = contentView.swipeRefresh
        netErrViewContainer = contentView.netErrorLayout
        progressBar = contentView.progressBar
        progressBar.visibility = showBar
        swipeRefreshLayout.setOnRefreshListener(::refresh)
        recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            //上拉加载
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!allLoadFlag && isSlideToBottom() && !swipeRefreshLayout.isRefreshing) {//上拉加载,
                    if (pageIndex != 0)
                        adapter.setFooter(RecAdapterWithFooter.STATUS_LOADING)
                    contentView.root.postDelayed({
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
    fun setAllLoaded() {
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
        Handler(Looper.getMainLooper()).postDelayed({
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
            loading = false
            stopRefreshing()
            notifyDataSetChanged()
            if (allLoad) {
                setAllLoaded()
            } else
                adapter.hideFooterView()
        }
    }

    fun failedToLoad() {
        stopRefreshing()
        showNetErr()
    }

    fun buildHeader(title: String, switchChecked: Boolean = false, lis: CompoundButton.OnCheckedChangeListener? = null) {
        val vb = ListHeaderWithSwitchBinding.inflate(layoutInflater)
        val headerTitle = vb.headerTitle
        val headerSwitch = vb.headerSwitch
        headerTitle.text = title
        headerSwitch.isChecked = switchChecked
        headerSwitch.setOnCheckedChangeListener(lis)
        vb.root.setOnClickListener { headerSwitch.toggle() }
        setHeader(vb.root)
    }

    private val defaultErrView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.net_error_layout, null, false)

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