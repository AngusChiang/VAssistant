package cn.vove7.jarvis.adapters

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.SettingChildItem
import cn.vove7.jarvis.view.custom.GroupItemHolder
import cn.vove7.jarvis.view.custom.SettingGroupItem
import cn.vove7.jarvis.view.tools.SettingItemHelper

/**
 * # SettingsExpandableAdapter
 *
 * @author 17719247306
 * 2018/9/10
 */
class SettingsExpandableAdapter(
        val context: Context,
        var groupItems: List<SettingGroupItem>
) : BaseExpandableListAdapter() {

//    private val animationHelper = ListItemAnimationHelper(false, 100f)

    var childHolders: SparseArray<Array<SettingItemHelper.ChildItemHolder?>> = SparseArray(groupCount)

    var groupHolders: Array<GroupItemHolder?> = arrayOfNulls(groupItems.size)

    init {
        for (i in 0 until groupCount) {
            childHolders.put(i, arrayOfNulls(getChildrenCount(i)))
        }
    }

    override fun getGroup(groupPosition: Int): SettingGroupItem = groupItems[groupPosition]

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false

    override fun hasStableIds(): Boolean = true

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val g = getGroup(groupPosition)
        var view = convertView
        val holder: GroupItemHolder = if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_group, null)
            GroupItemHolder(view).also {
                view!!.tag = it
                groupHolders[groupPosition] = it
            }
        } else view.tag as GroupItemHolder
        holder.lineView.setBackgroundResource(g.iconId)
        holder.titleView.text = g.title
        return view!!
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var view: View? = null
        try {
            view = childHolders[groupPosition][childPosition]?.itemView
        } catch (e: ArrayIndexOutOfBoundsException) {
        }

        if (view == null) {
            val c = getChild(groupPosition, childPosition)
            val holder = SettingItemHelper(context, c).fill()
            childHolders[groupPosition][childPosition] = holder
            view = holder.itemView
        }

        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int = groupItems[groupPosition].childItems.size

    override fun getChild(groupPosition: Int, childPosition: Int): SettingChildItem = groupItems[groupPosition].childItems[childPosition]

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = (groupPosition * 1000 + childPosition).toLong()

    override fun getGroupCount(): Int = groupItems.size
}