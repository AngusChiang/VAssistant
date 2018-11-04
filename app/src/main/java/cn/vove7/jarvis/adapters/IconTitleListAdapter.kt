package cn.vove7.jarvis.adapters

import android.content.Context
import android.util.SparseArray
import android.view.View
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.AboutActivity
import cn.vove7.vtp.easyadapter.BaseListAdapter

/**
 * # IconTitleListAdapter
 *
 * @author Administrator
 * 9/23/2018
 */
class IconTitleListAdapter(context: Context, dataset: List<IconTitleEntity>)
    : BaseListAdapter<AboutActivity.VH, IconTitleEntity>(context, dataset) {
    override fun layoutId(position: Int): Int = R.layout.item_whit_icon_title
    val holders = SparseArray<AboutActivity.VH>()
    override fun onBindView(holder: AboutActivity.VH, pos: Int, item: IconTitleEntity) {
        holders.put(pos, holder)
        item.iconId.let {
            if (it != null)
                holder.iconView.setImageResource(it)
        }
        holder.titleView.setText(item.titleId)
        val subtitle = item.summaryId
        if (subtitle == null) {
            holder.subTitleView.visibility = View.GONE
        } else {
            holder.subTitleView.visibility = View.VISIBLE
            holder.subTitleView.setText(subtitle)
        }
    }

    override fun onCreateViewHolder(view: View): AboutActivity.VH {
        return AboutActivity.VH(view)
    }
}

class IconTitleEntity(
        val iconId: Int? = null,
        val titleId: Int,
        val summaryId: Int? = null
)