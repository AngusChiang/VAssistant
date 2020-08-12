package cn.vove7.jarvis.view.tools

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.onClick
import cn.vove7.common.utils.spanColor
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.*
import cn.vove7.smartkey.BaseConfig
import cn.vove7.smartkey.android.set
import cn.vove7.common.utils.set
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.log.Vog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.color.ColorPalette
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.russhwolf.settings.contains
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar

/**
 * # SettingItemHelper
 *
 * @author 17719247306
 * 2018/9/10
 */
typealias OnClick = () -> Unit

@Suppress("unchecked_cast")
class SettingItemHelper(
        val context: Context,
        val settingItem: SettingChildItem,
        val config: BaseConfig
) {

    lateinit var holder: ChildItemHolder

    @SuppressLint("InflateParams")
    fun fill(p: ViewGroup? = null): ChildItemHolder {
        when (settingItem.itemType) {
            TYPE_INPUT -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, p, false)
                holder = ChildItemHolder(view)
                initAndSetInputListener()
                return holder
            }
            TYPE_SWITCH -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_switch, p, false)
                holder = SwitchItemHolder(view)
                initAndSetCompoundButtonListener()
                return holder
            }
            TYPE_CHECK_BOX -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_checkbox, p, false)
                holder = CheckBoxItemHolder(view)
                initAndSetCompoundButtonListener()
                return holder
            }
            TYPE_SINGLE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, p, false)
                holder = ChildItemHolder(view)
                initSingleDialog()
                return holder
            }
//            TYPE_MULTI -> {
//                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, p, false)
//                holder = ChildItemHolder(view)
//                initMultiDialog()
//                return holder
//            }
            TYPE_NUMBER -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, p, false)
                holder = ChildItemHolder(view)
                initNumberPickerDialog()
                return holder
            }
            TYPE_INTENT -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, p, false)
                holder = ChildItemHolder(view)
                initIntentItem()

                return holder
            }
            TYPE_COLOR -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, p, false)
                holder = ChildItemHolder(view)
                initColorPickerItem()

                return holder
            }
        }
        throw Exception("unknown type: ${settingItem.itemType}")
    }

    private fun initIntentItem() {
        setBasic {
            (settingItem.callback as CallbackOnSet<Any>?)?.invoke(ItemOperation(this), Any())
        }
    }

    private fun getPrefill(): String? {
        val d = settingItem.defaultValue.invoke() as String?
        var prefill: String? = d
        if (settingItem.keyId == null) {
            prefill = d
        } else {
            val key = settingItem.key ?: return d

            if (key in config.settings) {
                config.settings.getString(key).also {
                    if (it != "") settingItem.summary = it
                    prefill = it
                }
            }
        }
        return prefill
    }

    private fun initColorPickerItem() {
        val item = settingItem as ColorPickerItem
        val def = item.defaultValue() as Int?
        var color = item.key?.let {
            if (it in config)
                config.settings.getInt(it, def ?: 0)
            else def
        } ?: def
        val sum = {
            color?.let { "▇▇▇▇▇▇▇▇▇".spanColor(it) } ?: ""
        }
        item.summary = sum()

        setBasic {
            var selectedColor = color
            MaterialDialog(context).title(text = settingItem.title()).show {
//                val cs = ColorPalette.PrimarySub + ColorPalette.AccentSub
//                val ps = cs.fold(mutableListOf<Int>()) { list, arr -> arr.toCollection(list);list }

                colorChooser(
                        ColorPalette.Primary + ColorPalette.Accent,
                        ColorPalette.PrimarySub + ColorPalette.AccentSub,
//                        ps.toIntArray(),
                        initialSelection = color,
                        allowCustomArgb = true, showAlphaSelector = true,
                        waitForPositiveButton = false
                ) { _, color ->
                    getActionButton(WhichButton.POSITIVE).isEnabled = true
                    item.onChange?.invoke(color)
                    selectedColor = color
                }
                positiveButton {
                    color = selectedColor
                    if ((item.callback as CallbackOnSet<Int>?)?.invoke(ItemOperation(this@SettingItemHelper), color
                                ?: 0) != false) {
                        item.summary = sum()
                        if (item.keyId != null) {
                            this@SettingItemHelper.config[item.keyId] = selectedColor
                        }
                        setBasic()
                    }
                }
                getActionButton(WhichButton.POSITIVE).isEnabled = false
                negativeButton()
                onDismiss {
                    item.onDialogDismiss?.invoke()
                }
                item.onDialogShow?.invoke()
            }
        }
    }

    private fun initAndSetInputListener() {
        val backSummary: CharSequence? = settingItem.summary
            ?: settingItem.defaultValue.invoke() as String?

        //初始化summary
        getPrefill()
        if (settingItem.summary == null) {
            settingItem.summary = backSummary
        }
        setBasic {
            val prefill = getPrefill()
            MaterialDialog(context).title(text = settingItem.title()).input(prefill = prefill) { d, c ->
                Vog.d("initAndSetInputListener ---> $c")
                val s = c.toString()
                if ((settingItem.callback as CallbackOnSet<String>?)?.invoke(ItemOperation(this), s) != false) {
                    settingItem.summary = s
                    settingItem.keyId?.also {
                        config[settingItem.keyId] = s
                    }
                }
                setBasic()
            }.show {
                positiveButton()
                if ((settingItem as InputItem).clearable) {
                    neutralButton(text = "清空") {
                        if (settingItem.keyId != null) {
                            this@SettingItemHelper.config[settingItem.keyId] = null
                        }
                        settingItem.summary = backSummary
                        setBasic()
                    }
                }
                negativeButton()
            }
        }
    }

    /**
     * @param lis View.OnClickListener
     */
    private fun setBasic(lis: OnClick? = null) {
        holder.titleView.text = settingItem.title()
        if (settingItem.summary == null) {
            holder.summaryView.visibility = View.GONE
        } else {
            holder.summaryView.visibility = View.VISIBLE
            holder.summaryView.text = settingItem.summary
        }

        if (lis != null)
            holder.itemView.setOnClickListener {
                lis.invoke()
            }
    }

    /**
     *
     */
    private fun initAndSetCompoundButtonListener() {
        val item = settingItem
        val holder = holder as CompoundItemHolder
        setBasic { holder.compoundWight.toggle() }
        (item as CompoundItem).apply {
            if (onTileAreaClick != null) {
                holder.tileArea.onClick(onTileAreaClick)
            } else {
                holder.tileArea.background = null
            }
        }
        var lock = false
        if (item.keyId != null) {
            val b = config.settings.getBoolean(GlobalApp.getString(item.keyId), item.defaultValue.invoke() as Boolean)
            holder.compoundWight.isChecked = b
            holder.compoundWight.setOnCheckedChangeListener { _, isChecked ->
                if (lock) {
                    return@setOnCheckedChangeListener
                }
                if ((item.callback as CallbackOnSet<Boolean>?)?.invoke(ItemOperation(this), isChecked) != false) {
                    config[item.keyId] = isChecked
                } else {
                    lock = true
                    holder.compoundWight.toggle()
                    lock = false
                }
            }
        } else {//withoutSp
            holder.compoundWight.isChecked = item.defaultValue.invoke() as Boolean? ?: false
            holder.compoundWight.setOnCheckedChangeListener { _, isChecked ->
                (item.callback as CallbackOnSet<Boolean>?)?.invoke(ItemOperation(this), isChecked)
            }
        }
    }

    /**
     * 单选框初始化index
     * 支持key 保存String 和 index
     * @return Int
     */
    private fun getInitPosAndInitSummary(): Int {
        val item = settingItem as SingleChoiceItem
        val default = item.defaultValue.invoke() as Int? ?: -1
        if (default >= 0) {
            kotlin.runCatching {
                item.summary = item.choiceItems[default]
            }
        }
        val key = item.key
        key ?: return default
        val cs = config.settings
        if (key in cs) {
            val entity = item.choiceItems
            return try {
                val v = cs.getString(key)
                item.summary = v
                entity.indexOf(v)
            } catch (e: Exception) {//保存值为int
                val index = cs.getInt(key, -1)
                if (index >= 0) {
                    kotlin.runCatching {
                        item.summary = entity[index]
                    }
                }
                index
            }
        }
        return default
    }

    /**
     * 初始化单选对话框
     */
    private fun initSingleDialog() {
        val item = settingItem as SingleChoiceItem

        val ds = item.summary
        val items =
            if (item.keyId != null && item.entityArrId != null)
                context.resources.getStringArray(item.entityArrId).asList()
            else item.items!!

        var initp = getInitPosAndInitSummary()

        fun notifyData(pair: Pair<Int, String>?) {
            if ((item.callback as CallbackOnSet<Pair<Int, String>?>?)?.invoke(ItemOperation(this), pair) != false) {
                if (item.keyId != null) {
                    try {
                        config[item.keyId] = pair?.first
                    } catch (e: Exception) {
                        val key = item.key!!
                        config.settings.remove(key)
                        config[key] =  pair?.first
                    }
                }
                item.summary = pair?.second ?: ds
                setBasic()
            }
        }
        setBasic {
            MaterialDialog(context).title(text = item.title())
                    .listItemsSingleChoice(
                            items = items,
                            initialSelection = initp,
                            waitForPositiveButton = false
                    ) { d, i, t ->
                        //选择
                        d.dismiss()
                        if (i == initp) return@listItemsSingleChoice
                        initp = i
                        notifyData(i to t.toString())
                    }.show {
                        if (item.allowClear) {
                            neutralButton(text = "清空选择") {
                                initp = -1
                                notifyData(null)
                            }
                        }
                    }
        }
    }

//    /**
//     *
//     * @param holder ChildItemHolder
//     * @param item SettingChildItem
//     */
//    @Deprecated("unused")
//    private fun initMultiDialog() {
//        val item = settingItem
//        val sp = SpHelper(context)
//
//        setBasic()
//
//        MaterialDialog(context).title(text = item.title())
//                .listItemsMultiChoice(item.entityArrId) { _, _, ts ->
//                    if ((item.callback as CallbackOnSet<List<String>>?)?.invoke(ItemOperation(this), ts) != false) {
//                        if (item.keyId != null) {
//                            sp.set(item.keyId, ts)
//                        }
//                        item.summary = ts.toString()
//                        setBasic()
//                    }
//                    // callback
//                }.show()
//    }

    /**
     * 初始化 数字选择器
     */
    private fun initNumberPickerDialog() {
        val item = settingItem
        var old = if (item.keyId == null) item.defaultValue.invoke() as Int
        else config.settings.getInt(GlobalApp.getString(item.keyId), -1)
        item.summary = if (old == -1) {
            old = item.defaultValue.invoke() as Int
            item.summary ?: old.toString()
        } else {
            old.toString()
        }

        setBasic {
            val vv = buildNumberPickerView(context, (item as NumberPickerItem).range, old)
            MaterialDialog(context).title(text = item.title())
                    .customView(null, vv.first)
                    .positiveButton {
                        if ((item.callback as CallbackOnSet<Int>?)?.invoke(ItemOperation(this), old) != false) {
                            item.summary = old.toString()
                            if (item.keyId != null) {
                                config[item.keyId] = old
                            }
                            setBasic()
                        }
                    }
                    .onDismiss { (item as ItemDialogAction).onDialogDismiss?.invoke() }
                    .negativeButton()
                    .show()
            item.onDialogShow?.invoke()

            vv.second.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
                override fun onProgressChanged(seekBar: DiscreteSeekBar?, value: Int, fromUser: Boolean) {
                    if (fromUser) {
                        old = value
                        (settingItem as NumberPickerItem).onChange?.invoke(value)
                    }
                }

                override fun onStartTrackingTouch(seekBar: DiscreteSeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: DiscreteSeekBar?) {
                }
            })
        }
    }

    companion object {

        /**
         *
         * @param range Pair<Int, Int>
         * @param i Int
         * @return Pair<View, DiscreteSeekBar>
         */
        fun buildNumberPickerView(context: Context, range: Pair<Int, Int>, i: Int): Pair<View, DiscreteSeekBar> {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_num_picker, null)
            view.findViewById<TextView>(R.id.min_value_view).text = range.first.toString()
            view.findViewById<TextView>(R.id.max_value_view).text = range.second.toString()
            val sb = view.findViewById<DiscreteSeekBar>(R.id.seekbar)
            sb.min = range.first
            sb.max = range.second
            sb.progress = i

            return Pair(view, sb)
        }

    }

    open class ChildItemHolder(v: View) : BaseListAdapter.ViewHolder(v) {

        val titleView: TextView = v.findViewById(R.id.title)
        val summaryView: TextView = v.findViewById(R.id.summary)
    }

    class SwitchItemHolder(v: View) : CompoundItemHolder(v, v.findViewById(R.id.wight_switch))

    class CheckBoxItemHolder(v: View) : CompoundItemHolder(v, v.findViewById(R.id.wight_checkbox))

    open class CompoundItemHolder(
            v: View,
            val compoundWight: CompoundButton,
            val tileArea: View = v.findViewById(R.id.tile_area)
    ) : ChildItemHolder(v)
}