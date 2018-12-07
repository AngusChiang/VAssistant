package cn.vove7.jarvis.adapters

//package cn.vove7.ctassistant.adapter

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.show
import cn.vove7.jarvis.R
import cn.vove7.vtp.easyadapter.BaseListAdapter

class BottomListAdapter<Type>(val context: Context, items: List<ViewModel<Type>>,
                              private val listener: SimpleListAdapter.OnItemClickListener<Type>)
    : BaseListAdapter<BottomListAdapter.VHolder, ViewModel<Type>>(context, items) {

    override fun layoutId(position: Int): Int = R.layout.item_normal_icon_title

    override fun onCreateViewHolder(view: View): VHolder {
        return VHolder(view)
    }

    override fun onBindView(holder: VHolder, pos: Int, item: ViewModel<Type>) {
        holder.itemView.setOnClickListener { _ -> listener.onClick(null, pos, getItem(pos)) }
        holder.title.text = item.title
        if (item.icon != null) {
            holder.icon.visibility = View.VISIBLE
            holder.icon.setImageDrawable(item.icon)
        } else
            holder.icon.visibility = View.INVISIBLE
        if (item.subTitle != null && item.subTitle != "") {
            holder.subtitle.show()
            holder.subtitle.text = item.subTitle
        } else {
            holder.subtitle.gone()
        }
        holder.itemView.setOnClickListener {
            listener.onClick(null, pos, item)
        }

    }

    class VHolder(v: View)
        : BaseListAdapter.ViewHolder(v) {
        var icon: ImageView = v.findViewById(R.id.icon)
        var title: TextView = v.findViewById(R.id.title)
        var subtitle: TextView = v.findViewById(R.id.sub_title)

    }
}
