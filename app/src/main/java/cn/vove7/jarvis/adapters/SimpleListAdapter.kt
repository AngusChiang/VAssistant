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
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_normal_icon_title.view.*
import java.io.Serializable
import java.text.Collator

open class SimpleListAdapter<T>(
        private val dataset: MutableList<ListViewModel<T>>,
        val itemClickListener: OnItemClickListener<T>? = null,
        val checkable: Boolean = false
) : RecAdapterWithFooter<SimpleListAdapter.VHolder, ListViewModel<T>>() {

    private val holders = SparseArray<VHolder>()
    override fun itemCount(): Int = dataset.size
    override fun getItem(pos: Int): ListViewModel<T>? {
        return dataset[pos]
    }

    override fun onBindView(holder: VHolder, position: Int, item: ListViewModel<T>) {
        holders.put(position, holder)
        holder.title?.text = item.title

        when {
            item.icon != null -> {
                holder.icon?.visibility = View.VISIBLE
                holder.icon?.setImageDrawable(item.icon)
            }
            item.iconUrl != null -> {
                holder.icon?.visibility = View.VISIBLE
                Glide.with(holder.icon!!).load(item.iconUrl).into(holder.icon!!)
            }
            else -> {
                holder.icon?.visibility = View.INVISIBLE
            }
        }
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
                    Vog.d("OnCheckedChangeListener ---> $position ${item.title} $isChecked")
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
            if (it is AwesomeItem) {
                if (it.bgColor != null)
                    holder.itemView.setBackgroundColor(it.bgColor!!)
                it.onLoadDrawable(holder.icon!!)
            }
        }
    }

    val checkedList: List<ListViewModel<T>>
        get() = {
            val l = mutableListOf<ListViewModel<T>>()
//            for (k in holders) {
//
//            }
            l
        }.invoke()

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): VHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_normal_icon_title, parent, false)
        return VHolder(view)
    }

    class VHolder(v: View, adapter: RecAdapterWithFooter<RecViewHolder, *>? = null)
        : RecViewHolder(v, adapter) {
        val icon: ImageView = v.icon
        val title: TextView = v.title
        val subtitle: TextView = v.sub_title
        val checkBox: CheckBox = v.check_box

    }
    interface OnItemClickListener<T> {
        fun onClick(holder: VHolder?, pos: Int, item: ListViewModel<T>)
        fun onLongClick(holder: VHolder?, pos: Int, item: ListViewModel<T>): Boolean = false
        fun onItemCheckedStatusChanged(holder: VHolder?, item: ListViewModel<T>, isChecked: Boolean) {}
    }

}

class ListViewModel<T>(
        val title: String?,
        val subTitle: String? = null,
        var icon: Drawable? = null,
        val extra: T,
        var checked: Boolean = false,
        var iconUrl: String? = null
) : Serializable, Comparable<ListViewModel<T>> {

    companion object {
        var CollChina = Collator.getInstance(java.util.Locale.CHINA)!!
    }

    override fun compareTo(other: ListViewModel<T>): Int {

        val e = extra
        if (e is Comparable<*>) {
            return (e as Comparable<T>).compareTo(other.extra)
        }

        var tt1 = CollChina.getCollationKey(this.title)
        var tt2 = CollChina.getCollationKey(other.title)
        val c = CollChina.compare(tt1.sourceString, tt2.sourceString)
        return if (c == 0) {
            tt1 = CollChina.getCollationKey(this.subTitle)
            tt2 = CollChina.getCollationKey(other.subTitle)
            CollChina.compare(tt1.sourceString, tt2.sourceString)
        } else c
    }

    override fun toString(): String {
        return "ChoiceData(title='$title', subtitle=$subTitle, originalData=$extra)"
    }

}
