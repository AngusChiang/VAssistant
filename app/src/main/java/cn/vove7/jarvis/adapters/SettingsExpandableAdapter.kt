package cn.vove7.jarvis.adapters

import android.content.Context
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
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
class SettingsExpandableAdapter(val context: Context,
                                var groupItems: List<SettingGroupItem>, val expView: ExpandableListView)
    : BaseExpandableListAdapter() {

//    private val animationHelper = ListItemAnimationHelper(false, 100f)

    var childHolders: SparseArray<Array<SettingItemHelper.ChildItemHolder?>> = SparseArray(groupCount)

    private val transition by lazy {
        AutoTransition().apply {
            duration = 270
            interpolator = AccelerateDecelerateInterpolator()
        }
    }
    var groupHolders: Array<GroupItemHolder?> = arrayOfNulls(groupItems.size)

    init {
        for (i in 0 until groupCount) {
            childHolders.put(i, arrayOfNulls(getChildrenCount(i)))
        }
        expView.setOnGroupClickListener { parent, v, groupPosition, id ->
            TransitionManager.beginDelayedTransition(parent, transition)
            false
        }
    }


    /**
     * 获取group位置
     * @param g Int
     * @return Int
     */
    private fun getGroupAbsPos(g: Int): Int {
        var cc = 0
        for (i in 0 until g) {
            cc++
            if (expView.isGroupExpanded(i))
                cc += getChildrenCount(i)
        }
        return cc
    }

    /**
     * 获取child 在list中的位置
     * @param g Int
     * @param c Int
     * @return Int
     */
    private fun getChildAboPos(g: Int, c: Int): Int {
        var count = getGroupAbsPos(g)
        count += c + 1
        return count
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