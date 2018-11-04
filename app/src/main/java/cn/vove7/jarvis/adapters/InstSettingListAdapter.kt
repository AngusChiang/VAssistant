package cn.vove7.jarvis.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import cn.vove7.common.bridges.SettingsBridge
import cn.vove7.common.model.InstSettingItem
import cn.vove7.jarvis.view.*
import cn.vove7.jarvis.view.tools.SettingItemHelper

/**
 * # InstSettingListAdapter
 *
 * @author Administrator
 * 9/26/2018
 */
class InstSettingListAdapter(val context: Context, settingsName: String, val onFailed: () -> Unit) :
        BaseAdapter() {
    private val daset: MutableList<SettingChildItem> = mutableListOf()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val c = getItem(position)
        if (view == null) {
            val holder = SettingItemHelper(context).fill(c)!!
            view = holder.itemView
        }
        return view
    }

    override fun getItemId(position: Int): Long = position.toLong()

    private val settingsBridge = SettingsBridge(settingsName)

    init {
        val items = settingsBridge.instSettingItems
        if (items == null) {
            onFailed.invoke()
        } else
            items.forEach { entry ->
                parseItem(entry.key, entry.value).let {
                    if (it != null)
                        daset.add(it)
                }
            }
    }


    override fun getCount(): Int = daset.size


    override fun getItem(position: Int): SettingChildItem = daset[position]

    private fun parseItem(key: String, s: InstSettingItem): SettingChildItem? {
        try {
            return when (s.type) {
                InstSettingItem.TYPE_CHECK_BOX -> {
                    CheckBoxItem(title = s.title, summary = s.summary,
                            callback = { _, d ->
                                settingsBridge.set(key, d as Boolean)
                                return@CheckBoxItem false
                            }, defaultValue = {
                        settingsBridge.getBoolean(key) ?: s.defaultValue as Boolean? ?: false
                    })
                }
                InstSettingItem.TYPE_SWITCH -> {
                    SwitchItem(title = s.title, summary = s.summary, callback = { _, d ->
                        settingsBridge.set(key, d as Boolean)
                        return@SwitchItem false
                    }, defaultValue = {
                        settingsBridge.getBoolean(key) ?: s.defaultValue as Boolean? ?: false
                    })
                }
                InstSettingItem.TYPE_INT -> {
                    NumberPickerItem(title = s.title, summary = s.summary, callback = { _, d ->
                        settingsBridge.set(key, d as Int)
                        return@NumberPickerItem false
                    }, defaultValue = {
                        settingsBridge.getInt(key) ?: s.defaultValue as Int? ?: 0
                    }, range = Pair(s.range[0], s.range[1]))
                }
                InstSettingItem.TYPE_TEXT -> {
                    InputItem(title = s.title, summary = s.summary, callback = { _, d ->
                        settingsBridge.set(key, d)
                        return@InputItem false
                    }, defaultValue = {
                        settingsBridge.getString(key) ?: s.defaultValue as String? ?: ""
                    })
                }
                InstSettingItem.TYPE_SINGLE_CHOICE -> {
                    SingleChoiceItem(title = s.title, summary = s.summary, callback = { _, d ->
                        settingsBridge.set(key, (d as Pair<*, *>).second)
                        return@SingleChoiceItem true
                    }, defaultValue = {
                        val p = settingsBridge.getString(key)
                        when {
                            p != null -> s.items.indexOf(p)
                            s.defaultValue == null -> 0
                            else -> s.items.indexOf(s.defaultValue)
                        }
                    }, items = s.items.asList())
                }
                else -> null
            }
        } catch (e: Exception) {
            return null
        }
    }
}
