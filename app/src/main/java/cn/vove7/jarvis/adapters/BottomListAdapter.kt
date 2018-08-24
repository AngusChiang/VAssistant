package cn.vove7.jarvis.adapters

//package cn.vove7.ctassistant.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.vove7.jarvis.R
import cn.vove7.vtp.easyadapter.BaseListAdapter

class BottomListAdapter(val context: Context, items: MutableList<ViewModel>,
                        private val listener: SimpleListAdapter.OnItemClickListener)
    : BaseListAdapter<BottomListAdapter.VHolder, ViewModel>(context, items) {

    override fun layoutId(): Int = R.layout.item_normal_icon_title

    override fun onCreateViewHolder(view: View): VHolder {
        return VHolder(view)
    }

    override fun onBindView(holder: VHolder, pos: Int, item: ViewModel) {
        holder.itemView.setOnClickListener { _ -> listener.onItemClick(null, pos, getItem(pos)) }
        holder.title.text = item.title
        if (item.icon != null) {
            holder.icon.visibility = View.VISIBLE
            holder.icon.setImageDrawable(item.icon)
        } else
            holder.icon.visibility = View.INVISIBLE
        if (item.subTitle != null) {
            holder.subtitle.visibility = View.VISIBLE
            holder.subtitle.text = item.subTitle
        } else
            holder.subtitle.visibility = View.GONE
        holder.itemView.setOnClickListener {
            listener.onItemClick(null, pos, item)
        }

    }

    class VHolder(v: View)
        : BaseListAdapter.ViewHolder(v) {
        var icon: ImageView = v.findViewById(R.id.icon)
        var title: TextView = v.findViewById(R.id.title)
        var subtitle: TextView = v.findViewById(R.id.sub_title)

    }
}
