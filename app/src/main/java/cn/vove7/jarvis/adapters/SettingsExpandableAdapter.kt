package cn.vove7.jarvis.adapters

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import cn.vove7.common.app.AppConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.SettingChildItem
import cn.vove7.jarvis.view.custom.GroupItemHolder
import cn.vove7.jarvis.view.custom.SettingGroupItem
import cn.vove7.jarvis.view.dp
import cn.vove7.jarvis.view.tools.SettingItemHelper

/**
 * # SettingsExpandableAdapter
 *
 * @author 17719247306
 * 2018/9/10
 */
class SettingsExpandableAdapter(
        val context: Context,
        var groupItems: List<SettingGroupItem>,
        expView: ExpandableListView
) : BaseExpandableListAdapter() {

//    private val animationHelper = ListItemAnimationHelper(false, 100f)

    var childHolders: SparseArray<Array<SettingItemHelper.ChildItemHolder?>> = SparseArray(groupCount)

    var groupHolders: Array<GroupItemHolder?> = arrayOfNulls(groupItems.size)

    init {
        for (i in 0 until groupCount) {
            childHolders.put(i, arrayOfNulls(getChildrenCount(i)))
        }
        expView.setOnGroupCollapseListener { gPos ->
            if (gPos % 2 == 1) return@setOnGroupCollapseListener
            groupHolders[gPos / 2]?.downIcon?.apply {
                animate().rotation(0f).setDuration(200).start()
            }
        }
        expView.setOnGroupExpandListener { gPos ->
            if (gPos % 2 == 1) return@setOnGroupExpandListener
            groupHolders[gPos / 2]?.downIcon?.apply {
                animate()?.rotation(180f)?.setDuration(200)?.start()
            }
        }
    }

    override fun getGroup(groupPosition: Int): SettingGroupItem = groupItems[groupPosition]

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false

    override fun hasStableIds(): Boolean = true

    override fun getGroupView(g: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        if (g % 2 == 0) {
            val groupPosition = g / 2
            val item = getGroup(groupPosition)
            var view = convertView
            return (if (view == null || view.tag !is GroupItemHolder) {
                view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_group, parent, false)
                GroupItemHolder(view).also {
                    view.tag = it
                    groupHolders[groupPosition] = it
                }
            } else view.tag as GroupItemHolder).let {
                it.lineView.setBackgroundResource(item.iconId)
                it.titleView.text = item.title
                it.itemView
            }
        } else {
            //分隔符
            return View(parent?.context).also {
                it.background = null
                it.isEnabled = false
                it.layoutParams = ViewGroup.LayoutParams(-1, 5.dp.px)
            }
        }
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var view: View? = null
        try {
            view = childHolders[groupPosition][childPosition]?.itemView
        } catch (e: ArrayIndexOutOfBoundsException) {
        }

        if (view == null) {
            val c = getChild(groupPosition, childPosition)
            val holder = SettingItemHelper(context, c, AppConfig).fill()
            childHolders[groupPosition][childPosition] = holder
            view = holder.itemView
        }

        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int = if (groupPosition % 2 == 0) groupItems[groupPosition / 2].childItems.size else 0

    override fun getChild(groupPosition: Int, childPosition: Int): SettingChildItem = groupItems[groupPosition / 2].childItems[childPosition]

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = (groupPosition * 1000 + childPosition).toLong()

    override fun getGroupCount(): Int = groupItems.size * 2 - 1 // 空隙
}