package cn.vove7.jarvis.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog

/**
 *
 *
 * Created by 17719 on 2018/8/13
 */
abstract class RecAdapterWithFooter<V : RecAdapterWithFooter.RecViewHolder> : RecyclerView.Adapter<RecAdapterWithFooter.RecViewHolder>() {

    override fun getItemCount(): Int {
        return itemCount() + 1
    }

    abstract fun getItem(pos: Int): Any?

    abstract fun itemCount(): Int
    override fun onBindViewHolder(holder: RecAdapterWithFooter.RecViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_ITEM -> {
                onBindView(holder as V, position, getItem(position))
            }
            TYPE_FOOTER -> {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecViewHolder {
        return when (viewType) {
            TYPE_FOOTER -> {
                val footerView = LayoutInflater.from(parent.context).inflate(R.layout.footer_layout,
                        parent, false)
                RecViewHolder(footerView, this as RecAdapterWithFooter<RecViewHolder>)
            }
            else -> {
                onCreateHolder(parent, viewType)
            }
        }
    }

    abstract fun onCreateHolder(parent: ViewGroup, viewType: Int): V


    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            TYPE_FOOTER
        } else {
            TYPE_ITEM
        }
    }

    companion object {
        val TYPE_ITEM = 1
        val TYPE_FOOTER = 2

        val STATUS_LOADING = 0
        val STATUS_NET_ERROR = 1
        val STATUS_ALL_LOAD = 2
    }

    abstract fun onBindView(holder: V, position: Int, item: Any?)

    private var footerView: View? = null
    private lateinit var llLoading: LinearLayout  // 正在加载view
    private lateinit var llLoadError: LinearLayout // 错误view
    private lateinit var llLoadedAll: LinearLayout // 加载全部view
    private lateinit var errorTextOfFooter: TextView
    fun hideFooterView() {
        Vog.d(this, "hideFooterView footerView: ${footerView != null}")
        footerView?.visibility = View.GONE
    }

    fun setFooter(status: Int) {
        if (footerView == null)
            return
        footerView?.visibility = View.VISIBLE
        when (status) {
            STATUS_LOADING -> {
                llLoadedAll.visibility = View.GONE
                llLoadError.visibility = View.GONE
                llLoading.visibility = View.VISIBLE
            }
            STATUS_NET_ERROR -> {
                errorTextOfFooter.setText(R.string.no_net_tapme)
                llLoadedAll.visibility = View.GONE
                llLoadError.visibility = View.VISIBLE
                llLoading.visibility = View.GONE
            }
            STATUS_ALL_LOAD -> {
                llLoadedAll.visibility = View.VISIBLE
                llLoadError.visibility = View.GONE
                llLoading.visibility = View.GONE
            }
        }
    }

    open class RecViewHolder(view: View, adapter: RecAdapterWithFooter<RecViewHolder>?) : RecyclerView.ViewHolder(view) {
        init {
            if (adapter != null) {
                adapter.footerView = view
                adapter.llLoading = view.findViewById(R.id.ll_footer_loading)
                adapter.llLoadError = view.findViewById(R.id.ll_footer_error)
                adapter.errorTextOfFooter = view.findViewById(R.id.error_text)
                adapter.llLoadedAll = view.findViewById(R.id.ll_footer_all_loaded)
                adapter.hideFooterView()
            }
        }


    }
}