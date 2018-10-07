package cn.vove7.jarvis.view.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.*
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar

/**
 * # SettingItemHelper
 *
 * @author 17719247306
 * 2018/9/10
 */
typealias OnClick = () -> Unit

class SettingItemHelper(val context: Context) {

    @SuppressLint("InflateParams")
    fun fill(childItem: SettingChildItem): ChildItemHolder? {
        when (childItem.itemType) {
            TYPE_INPUT -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, null)
                val holder = ChildItemHolder(view!!)
                initAndSetInputListener(holder, childItem)
                return holder
            }
            TYPE_SWITCH -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_switch, null)
                val holder = SwitchItemHolder(view!!)
                initAndSetCompoundButtonListener(holder, childItem)
                return holder
            }
            TYPE_SWITCH_CALLBACK -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_switch, null)
                val holder = SwitchItemHolder(view!!)
                initAndSetCompoundButtonListener(holder, childItem)
                return holder
            }
            TYPE_CHECK_BOX -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_checkbox, null)
                val holder = CheckBoxItemHolder(view!!)
                initAndSetCompoundButtonListener(holder, childItem)
                return holder
            }
            TYPE_SINGLE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, null)
                val holder = ChildItemHolder(view!!)
                initSingleDialog(holder, childItem)
                return holder
            }
            TYPE_MULTI -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, null)
                val holder = ChildItemHolder(view!!)
                initMultiDialog(holder, childItem)
                return holder
            }
            TYPE_NUMBER -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, null)
                val holder = ChildItemHolder(view!!)
                initNumberPickerDialog(holder, childItem)
                return holder
            }
            TYPE_INTENT -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, null)
                val holder = ChildItemHolder(view!!)
                initIntentItem(holder, childItem)
                return holder
            }
        }
        return null
    }

    private fun initIntentItem(holder: ChildItemHolder, item: SettingChildItem) {
        setBasic(holder, item) {
            item.callback?.invoke(holder, Any())
        }
    }

    private fun initAndSetInputListener(holder: ChildItemHolder, item: SettingChildItem) {
        val sp = SpHelper(context)
        item.summary = if (item.keyId != null) sp.getString(item.keyId)
        else item.defaultValue.invoke() as String
        setBasic(holder, item) {
            val pre = if (item.keyId != null) sp.getString(item.keyId) else item.defaultValue.invoke() as String
            MaterialDialog(context).title(text = item.title()).input(prefill = pre) { d, c ->
                Vog.d(this, "initAndSetInputListener ---> $c")
                val s = c.toString()
                if (item.keyId != null)
                    sp.set(item.keyId, s)
                item.summary = s
                item.callback?.invoke(holder, s)
                setBasic(holder, item)
            }.show()
        }
    }

    /**
     *
     * @param holder ChildItemHolder
     * @param item SettingChildItem
     * @param lis View.OnClickListener
     */
    private fun setBasic(holder: ChildItemHolder, item: SettingChildItem, lis: OnClick? = null) {
        holder.titleView.text = item.title()
        if (item.summary == null) {
            holder.summaryView.visibility = View.GONE
        } else {
            holder.summaryView.visibility = View.VISIBLE
            holder.summaryView.text = item.summary
        }

        if (lis != null)
            holder.itemView.setOnClickListener {
                lis.invoke()
            }
    }

    /**
     *
     * @param holder SwitchItemHolder
     * @param item SettingsActivity.SettingChildItem
     * @param withoutSp Boolean
     */
    private fun initAndSetCompoundButtonListener(holder: CompoundItemHolder, item: SettingChildItem) {
        setBasic(holder, item) { holder.compoundWight.toggle() }

        if (item.keyId != null) {
            val sp = SpHelper(context)
            val b = sp.getBoolean(item.keyId, item.defaultValue.invoke() as Boolean)
            holder.compoundWight.isChecked = b
            holder.compoundWight.setOnCheckedChangeListener { _, isChecked ->
                if (item.keyId != null)
                    sp.set(item.keyId, isChecked)
                item.callback?.invoke(holder, isChecked)
            }
        } else {//withoutSp
            holder.compoundWight.isChecked = item.defaultValue.invoke() as Boolean? ?: false
            holder.compoundWight.setOnCheckedChangeListener { _, isChecked ->
                item.callback?.invoke(holder, isChecked)
            }
        }
    }

    /**
     *
     * @param holder ChildItemHolder
     * @param item SettingChildItem
     */
    private fun initSingleDialog(holder: ChildItemHolder, item: SettingChildItem) {
        val sp = SpHelper(context)

        var initPos = if (item.keyId != null) {
            val v = sp.getString(item.keyId)
            val entity = context.resources.getStringArray(item.entityArrId!!)
            if (v != null) {
                item.summary = v
                entity.indexOf(v)
            } else 0
        } else {
            val i = item.defaultValue.invoke() as Int? ?: 0
            item.summary = item.items?.get(i)
            i
        }

        setBasic(holder, item) {
            MaterialDialog(context)
                    .title(text = item.title())
                    .listItemsSingleChoice(items =
                    if (item.keyId != null) context.resources.getStringArray(item.entityArrId!!).asList()
                    else item.items, initialSelection = initPos) { _, i, t ->
                        if (item.keyId != null)
                            sp.set(item.keyId, t)
                        item.summary = t
                        setBasic(holder, item)
                        initPos = i
                        item.callback?.invoke(holder, Pair(i, t))
                    }.show()
        }


    }

    /**
     *
     * @param holder ChildItemHolder
     * @param item SettingChildItem
     */
    @Deprecated("unused")
    private fun initMultiDialog(holder: ChildItemHolder, item: SettingChildItem) {
        val sp = SpHelper(context)
        val entity = context.resources.getStringArray(item.entityArrId!!)

        val v = sp.getString(item.keyId!!) ?: item.defaultValue.invoke()

        setBasic(holder, item)

        MaterialDialog(context).title(text = item.title())
                .listItemsMultiChoice(item.entityArrId) { d, iss, ts ->
                    if (item.keyId != null)
                        sp.set(item.keyId, ts)
                    item.summary = ts.toString()
                    setBasic(holder, item)
                    // callback
                    item.callback?.invoke(holder, ts)
                }.show()
    }

    /**
     *
     * @param holder ChildItemHolder
     * @param item SettingChildItem
     */
    private fun initNumberPickerDialog(holder: ChildItemHolder, item: SettingChildItem) {
        val sp = SpHelper(context)
        var old = if (item.keyId == null) item.defaultValue.invoke() as Int
        else sp.getInt(item.keyId)
        if (old == -1) old = item.defaultValue.invoke() as Int
        item.summary = old.toString()

        setBasic(holder, item) {
            val vv = buildNumberPickerView(item.range!!, old)
            MaterialDialog(context).title(text = item.title())
                    .customView(null, vv.first)
                    .positiveButton {
                        item.summary = old.toString()
                        if (item.keyId != null)
                            sp.set(item.keyId, old)
                        setBasic(holder, item)
                        item.callback?.invoke(holder, old)
                    }
                    .show()

            vv.second.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
                override fun onProgressChanged(seekBar: DiscreteSeekBar?, value: Int, fromUser: Boolean) {
                    if (fromUser) {
                        old = value
                    }
                }

                override fun onStartTrackingTouch(seekBar: DiscreteSeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: DiscreteSeekBar?) {
                }
            })
        }
    }

    /**
     *
     * @param range Pair<Int, Int>
     * @param i Int
     * @return Pair<View, DiscreteSeekBar>
     */
    private fun buildNumberPickerView(range: Pair<Int, Int>, i: Int): Pair<View, DiscreteSeekBar> {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_num_picker, null)
        view.findViewById<TextView>(R.id.min_value_view).text = range.first.toString()
        view.findViewById<TextView>(R.id.max_value_view).text = range.second.toString()
        val sb = view.findViewById<DiscreteSeekBar>(R.id.seekbar)
        sb.min = range.first
        sb.max = range.second
        sb.progress = i

        return Pair(view, sb)
    }

    open class ChildItemHolder(v: View) : BaseListAdapter.ViewHolder(v) {

        val titleView: TextView = v.findViewById(R.id.title)
        val summaryView: TextView = v.findViewById(R.id.summary)
    }

    class SwitchItemHolder(v: View) : CompoundItemHolder(v, v.findViewById(R.id.wight_switch))

    class CheckBoxItemHolder(v: View) : CompoundItemHolder(v, v.findViewById(R.id.wight_checkbox))

    open class CompoundItemHolder(v: View, val compoundWight: CompoundButton) : ChildItemHolder(v)
}