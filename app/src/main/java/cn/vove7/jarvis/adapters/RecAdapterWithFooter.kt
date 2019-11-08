package cn.vove7.jarvis.adapters

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog

/**
 *
 *
 * Created by 17719 on 2018/8/13
 */
abstract class RecAdapterWithFooter<V : RecAdapterWithFooter.RecViewHolder,DataType>
    : androidx.recyclerview.widget.RecyclerView.Adapter<RecAdapterWithFooter.RecViewHolder>() {

    override fun getItemCount(): Int {
        return itemCount() + 1
    }

    abstract fun getItem(pos: Int): DataType?

    abstract fun itemCount(): Int
    override fun onBindViewHolder(holder: RecAdapterWithFooter.RecViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_ITEM -> {
                try {
                    onBindView(holder as V, position, getItem(position)!!)
                } catch (e: ClassCastException) {
                    GlobalLog.err(e.message)
                }
            }
            TYPE_FOOTER -> {

            }
        }
        runEnterAnimation(holder.itemView, holder.adapterPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecViewHolder {
        return when (viewType) {
            TYPE_FOOTER -> {
                val footerView = LayoutInflater.from(parent.context).inflate(R.layout.footer_layout,
                        parent, false)
                RecViewHolder(footerView, this as RecAdapterWithFooter<RecViewHolder,*>)
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

    var status = STATUS_HIDDEN

    companion object {
        const val TYPE_ITEM = 1
        const val TYPE_FOOTER = 2

        const val STATUS_HIDDEN = -1
        const val STATUS_LOADING = 0
        const val STATUS_NET_ERROR = 1
        const val STATUS_ALL_LOAD = 2
    }

    abstract fun onBindView(holder: V, position: Int, item: DataType)

    private var footerView: View? = null
    private lateinit var llLoading: LinearLayout  // 正在加载view
    private lateinit var llLoadError: LinearLayout // 错误view
    private lateinit var llLoadedAll: LinearLayout // 加载全部view
    private lateinit var errorTextOfFooter: TextView
    fun hideFooterView() {
        status = STATUS_HIDDEN
        Vog.d("hideFooterView footerView: ${footerView != null}")
        footerView?.visibility = View.GONE
    }

    fun setFooter(status: Int) {
        Vog.d("setFooter ---> $status")
        this.status = status
        if (footerView == null)
            return
        footerView?.visibility = View.VISIBLE
        when (status) {
            STATUS_HIDDEN -> {
                hideFooterView()
            }
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

    open class RecViewHolder(view: View, adapter: RecAdapterWithFooter<RecViewHolder,*>?) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        init {
            if (adapter != null) {
                adapter.footerView = view
                adapter.llLoading = view.findViewById(R.id.ll_footer_loading)
                adapter.llLoadError = view.findViewById(R.id.ll_footer_error)
                adapter.errorTextOfFooter = view.findViewById(R.id.error_text)
                adapter.llLoadedAll = view.findViewById(R.id.ll_footer_all_loaded)
//                adapter.hideFooterView()
                adapter.setFooter(adapter.status)
            }
        }
    }

    private var lastAnimatedPosition = 0
    var animationsLocked = false
    private val delayEnterAnimation = true

    private fun runEnterAnimation(view: View, position: Int) {
        if (animationsLocked)
            return

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position
            view.translationY = 50f//相对于原始位置下方dy
            view.alpha = 0f//完全透明
            //每个item项两个动画，从透明到不透明，从下方移动到原来的位置
            //并且根据item的位置设置延迟的时间，达到一个接着一个的效果
            view.animate()
                    .translationY(0.5f).alpha(1f)//设置最终效果为完全不透明，并且在原来的位置
                    .setStartDelay((10 * position).toLong())//根据item的位置设置延迟时间，达到依次动画一个接一个进行的效果
                    .setInterpolator(DecelerateInterpolator(0.5f))//设置动画效果为在动画开始的地方快然后慢
                    .setDuration(300)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            animationsLocked = true//确保仅屏幕一开始能够显示的item项才开启动画，也就是说屏幕下方还没有显示的item项滑动时是没有动画效果
                        }
                    }).start()
        }
    }

}