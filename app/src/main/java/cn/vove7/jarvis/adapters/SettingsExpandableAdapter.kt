package cn.vove7.jarvis.adapters

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import cn.vassistant.plugininterface.app.GlobalLog
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.SettingChildItem
import cn.vove7.jarvis.view.animation.ListItemAnimationHelper
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

    private val animationHelper = ListItemAnimationHelper(false, 100f)

    var childHolders: SparseArray<Array<SettingItemHelper.ChildItemHolder?>> = SparseArray(groupCount)

    var groupHolders: Array<GroupItemHolder?> = arrayOfNulls(groupItems.size)

    init {
        for (i in 0 until groupCount) {
            childHolders.put(i, arrayOfNulls(getChildrenCount(i)))
        }

        expView.setOnGroupCollapseListener { gPos ->//child item 立即消失
            //收缩
            animationHelper.init()
            getDy(gPos).let {
                if (it != 0)
                    animationHelper.dy = it.toFloat()
            }
            //groups
            try {
                for (i in gPos until groupCount) {
                    if (i != gPos) {
                        val gpos = getGroupAbsPos(i)
//                    Vog.d(this, "Collapse group ---> $i         $gpos")
                        animationHelper.fromB2T(groupHolders[i]?.itemView, gpos)
                    }
                    if (i == gPos || expView.isGroupExpanded(i))//if展开
                        for (j in 0 until getChildrenCount(i)) {
                            val cpos = getChildAboPos(i, j)
//                        Vog.d(this, "Collapse child ---> $i $j     $cpos")
                            if (i == gPos)//消失行
                                animationHelper.hide(childHolders[i][j]?.itemView)
                            else animationHelper.fromB2T(childHolders[i][j]?.itemView, cpos)
                        }
                }
                groupHolders[gPos]!!.downIcon.animate().rotation(0f).setDuration(200).start()
            }catch (e: Exception) {
                GlobalLog.err(e)
            }
        }
        expView.setOnGroupExpandListener { gPos ->
            animationHelper.init()
            getDy(gPos).let {
                if (it != 0)
                    animationHelper.dy = it.toFloat()
            }
            //groups
            try {
                for (i in gPos until groupCount) {
                    if (gPos != i) {
//                    Vog.d(this, "Expand group ---> $i")
                        animationHelper.fromT2B(groupHolders[i]?.itemView, getGroupAbsPos(i))
                    }
                    if (expView.isGroupExpanded(i)) {
                    }//if展开
                    for (j in 0 until getChildrenCount(i)) {
                        val cv = childHolders[i][j]?.itemView
                        if (cv != null) {//maybe 未加载
                            if (i == gPos) {//展开行
                                animationHelper.fromT2B(cv, getChildAboPos(i, j), true, 50f)
                            } else {
                                animationHelper.fromT2B(cv, getChildAboPos(i, j))
                            }
                        }
//                        Vog.d(this, "Expand child ---> $i $j")
                    }
                }
            } catch (e: Exception) {
                GlobalLog.err(e)
            }

            groupHolders[gPos]?.downIcon?.animate()?.rotation(180f)?.setDuration(200)?.start()
        }
    }

    private fun getDy(groupPosition: Int): Int {
        var d = 0
        for (i in childHolders[groupPosition]) {
            if (i != null) d += i.itemView.height
        }
        return d
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

        var firLoad = true
        if (view == null) {
            val c = getChild(groupPosition, childPosition)
            val holder = SettingItemHelper(context).fill(c)!!
            childHolders[groupPosition][childPosition] = holder
            view = holder.itemView
            animationHelper.fromT2B(view, childPosition)
        } else firLoad = false
        if (firLoad && expView.isGroupExpanded(groupPosition))//首次显示
            animationHelper.fromT2B(view, getChildAboPos(groupPosition, childPosition), true, 50f)


        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int = groupItems[groupPosition].childItems.size

    override fun getChild(groupPosition: Int, childPosition: Int): SettingChildItem = groupItems[groupPosition].childItems[childPosition]

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = (groupPosition * 1000 + childPosition).toLong()

    override fun getGroupCount(): Int = groupItems.size
}