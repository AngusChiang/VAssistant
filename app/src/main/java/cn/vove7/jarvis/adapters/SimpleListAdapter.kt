package cn.vove7.jarvis.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.jarvis.R

class SimpleListAdapter(private val dataset: MutableList<ViewModel>
                        , val itemClickListener: OnItemClickListener? = null) : RecAdapterWithFooter<SimpleListAdapter.VHolder>() {

    override fun itemCount(): Int = dataset.size
    override fun getItem(pos: Int): Any? {
        return dataset[pos]
    }

    override fun onBindView(holder: VHolder, position: Int, item: Any?) {
        val item = item as ViewModel
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
            itemClickListener?.onItemClick(holder, position, item)
        }

    }

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): VHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_normal_icon_title, parent, false)
        return VHolder(view)
    }

    interface OnItemClickListener {
        fun onItemClick(holder: VHolder?, pos: Int, item: ViewModel)
    }


    class VHolder(v: View, adapter: RecAdapterWithFooter<RecAdapterWithFooter.RecViewHolder>? = null)
        : RecAdapterWithFooter.RecViewHolder(v, adapter) {
        var icon: ImageView? = null
        var title: TextView? = null
        var subtitle: TextView? = null

        init {
            if (adapter == null) {
                icon = v.findViewById(R.id.icon)
                title = v.findViewById(R.id.title)
                subtitle = v.findViewById(R.id.sub_title)
            }
        }

    }
}

class ViewModel(
        val title: String,
        val subTitle: String? = null,
        val icon: Drawable? = null,
        val extra: Any? = null
)