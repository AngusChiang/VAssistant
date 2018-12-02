package cn.vove7.jarvis.adapters

import android.graphics.drawable.Drawable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.jarvis.R
import cn.vove7.jarvis.fragments.AwesomeItem
import cn.vove7.vtp.log.Vog

open class SimpleListAdapter<T>(private val dataset: MutableList<ViewModel<T>>
                                , val itemClickListener: OnItemClickListener<T>? = null,
                                val checkable: Boolean = false)
    : RecAdapterWithFooter<SimpleListAdapter.VHolder, ViewModel<T>>() {

    private val holders = SparseArray<SimpleListAdapter.VHolder>()
    override fun itemCount(): Int = dataset.size
    override fun getItem(pos: Int): ViewModel<T>? {
        return dataset[pos]
    }

    override fun onBindView(holder: VHolder, position: Int, item: ViewModel<T>) {
        holders.put(position, holder)
        holder.title?.text = item.title

        if (item.icon != null) {
            holder.icon?.visibility = View.VISIBLE
            holder.icon?.setImageDrawable(item.icon)
        } else
            holder.icon!!.visibility = View.INVISIBLE
        if (item.subTitle != null) {
            holder.subtitle?.visibility = View.VISIBLE
            holder.subtitle?.text = item.subTitle
        } else
            holder.subtitle?.visibility = View.GONE
        holder.itemView.setOnClickListener {
            itemClickListener?.onClick(holder, position, item)
        }
        holder.itemView.setOnLongClickListener {
            itemClickListener?.onLongClick(holder, position, item) == true
        }
        if (checkable) {
            holder.checkBox?.visibility = View.VISIBLE
            holder.checkBox?.isChecked = item.checked
            holder.checkBox?.setOnCheckedChangeListener { b, isChecked ->
                if (b.isPressed) {//手动按时才执行
                    Vog.d(this, "OnCheckedChangeListener ---> $position ${item.title} $isChecked")
                    item.checked = isChecked//更新状态
                    itemClickListener?.onItemCheckedStatusChanged(holder, item, isChecked)
                }
            }
        } else {
            holder.checkBox?.visibility = View.GONE
            holder.checkBox?.setOnCheckedChangeListener(null)
        }

        //背景色
        item.extra.also {
            if (it is AwesomeItem && it.bgColor != null) {
                holder.itemView.setBackgroundColor(it.bgColor!!)
            }
        }
    }

    val checkedList: List<ViewModel<T>>
        get() = {
            val l = mutableListOf<ViewModel<T>>()
//            for (k in holders) {
//
//            }
            l
        }.invoke()

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): VHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_normal_icon_title, parent, false)
        return VHolder(view)
    }

    class VHolder(v: View, adapter: RecAdapterWithFooter<RecAdapterWithFooter.RecViewHolder, *>? = null)
        : RecAdapterWithFooter.RecViewHolder(v, adapter) {
        var icon: ImageView? = null
        var title: TextView? = null
        var subtitle: TextView? = null
        var checkBox: CheckBox? = null

        init {
            if (adapter == null) {
                icon = v.findViewById(R.id.icon)
                title = v.findViewById(R.id.title)
                subtitle = v.findViewById(R.id.sub_title)
                checkBox = v.findViewById(R.id.check_box)
            }
        }

    }

    interface OnItemClickListener<T> {
        fun onClick(holder: VHolder?, pos: Int, item: ViewModel<T>)
        fun onLongClick(holder: VHolder?, pos: Int, item: ViewModel<T>): Boolean = false
        fun onItemCheckedStatusChanged(holder: VHolder?, item: ViewModel<T>, isChecked: Boolean) {}
    }

}

class ViewModel<T>(
        val title: String?,
        val subTitle: String? = null,
        val icon: Drawable? = null,
        val extra: T,
        var checked: Boolean = false
)
