package cn.vove7.jarvis.view.tools

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import cn.vove7.common.utils.ThreadPool
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.AppConfig
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
            (item.callback as CallbackOnSet<Any>?)?.invoke(holder, Any())
        }
    }

    private fun initAndSetInputListener(holder: ChildItemHolder, item: SettingChildItem) {
        val sp = SpHelper(context)
        val backSummary: String? = item.summary
        val d = item.defaultValue.invoke() as String?
        var prefill: String? = null
        if (item.keyId == null) {
            prefill = d
        } else {
            sp.getString(item.keyId).also {
                if (it != null && it != "") item.summary = it
                prefill = it
            }
        }
//        item.summary = if (item.keyId != null) sp.getString(item.keyId)
//            ?: d.let { if (it == null || it == "") item.summary else it }
//        else d.let { if (it == null || it == "") item.summary else it }
//        val prefill = if (item.keyId != null) sp.getString(item.keyId)
        setBasic(holder, item) {
            MaterialDialog(context).title(text = item.title()).input(prefill = prefill) { d, c ->
                Vog.d("initAndSetInputListener ---> $c")
                val s = c.toString()
                if ((item.callback as CallbackOnSet<String>?)?.invoke(holder, s) != false) {
                    if (item.keyId != null) {
                        sp.set(item.keyId, s)
                        loadConfigInCacheThread()
                    }
                    item.summary = s
                }
                setBasic(holder, item)
            }.show {
                positiveButton()
                neutralButton(text = "清空") {
                    if (item.keyId != null) {
                        sp.setStringNull(context.getString(item.keyId))
                        loadConfigInCacheThread()
                    }
                    item.summary = backSummary
                    setBasic(holder, item)
                }
                negativeButton()
            }
        }
    }

    private fun loadConfigInCacheThread() {
        ThreadPool.runOnCachePool {
            AppConfig.reload()
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
                if ((item.callback as CallbackOnSet<Boolean>?)?.invoke(holder, isChecked) != false) {
                    sp.set(item.keyId, isChecked)
                    loadConfigInCacheThread()
                }
            }
        } else {//withoutSp
            holder.compoundWight.isChecked = item.defaultValue.invoke() as Boolean? ?: false
            holder.compoundWight.setOnCheckedChangeListener { _, isChecked ->
                (item.callback as CallbackOnSet<Boolean>?)?.invoke(holder, isChecked)
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
        val items =
            if (item.keyId != null) {
                if (item.entityArrId != null)
                    context.resources.getStringArray(item.entityArrId).asList()
                else item.items
            } else item.items

        setBasic(holder, item) {
            MaterialDialog(context)
                    .title(text = item.title())
                    .listItemsSingleChoice(items = items, initialSelection = initPos) { _, i, t ->
                        if ((item.callback as CallbackOnSet<Pair<Int, String>>?)?.invoke(holder, Pair(i, t)) != false) {
                            if (item.keyId != null) {
                                sp.set(item.keyId, t)
                                loadConfigInCacheThread()
                            }
                            item.summary = t
                            setBasic(holder, item)
                            initPos = i
                        }
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

        val v = if (item.keyId != null) sp.getString(item.keyId) else item.defaultValue.invoke()

        setBasic(holder, item)

        MaterialDialog(context).title(text = item.title())
                .listItemsMultiChoice(item.entityArrId) { d, iss, ts ->
                    if ((item.callback as CallbackOnSet<List<String>>?)?.invoke(holder, ts) != false) {
                        if (item.keyId != null) {
                            sp.set(item.keyId, ts)
                            loadConfigInCacheThread()
                        }
                        item.summary = ts.toString()
                        setBasic(holder, item)
                    }
                    // callback
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
        if (old == -1) {
            old = item.defaultValue.invoke() as Int
            item.summary = item.summary ?: old.toString()
        } else
            item.summary = old.toString()

        setBasic(holder, item) {
            val vv = buildNumberPickerView(item.range!!, old)
            MaterialDialog(context).title(text = item.title())
                    .customView(null, vv.first)
                    .positiveButton {
                        if ((item.callback as CallbackOnSet<Int>?)?.invoke(holder, old) != false) {
                            item.summary = old.toString()
                            if (item.keyId != null) {
                                sp.set(item.keyId, old)
                                loadConfigInCacheThread()
                            }
                            setBasic(holder, item)
                        }
                    }
                    .negativeButton()
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