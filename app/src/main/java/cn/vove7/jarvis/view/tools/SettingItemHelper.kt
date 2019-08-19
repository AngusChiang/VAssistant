package cn.vove7.jarvis.view.tools

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.set
import cn.vove7.common.utils.ThreadPool
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.*
import cn.vove7.smartkey.android.set
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsMultiChoice
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
        val settingItem: SettingChildItem
) {

    lateinit var holder: ChildItemHolder

    @SuppressLint("InflateParams")
    fun fill(): ChildItemHolder {
        when (settingItem.itemType) {
            TYPE_INPUT -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, null)
                holder = ChildItemHolder(view)
                initAndSetInputListener()
                return holder
            }
            TYPE_SWITCH -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_switch, null)
                holder = SwitchItemHolder(view)
                initAndSetCompoundButtonListener()
                return holder
            }
            TYPE_SWITCH_CALLBACK -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_switch, null)
                holder = SwitchItemHolder(view)
                initAndSetCompoundButtonListener()

                return holder
            }
            TYPE_CHECK_BOX -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_checkbox, null)
                holder = CheckBoxItemHolder(view)
                initAndSetCompoundButtonListener()
                return holder
            }
            TYPE_SINGLE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, null)
                holder = ChildItemHolder(view)
                initSingleDialog()
                return holder
            }
            TYPE_MULTI -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, null)
                holder = ChildItemHolder(view)
                initMultiDialog()
                return holder
            }
            TYPE_NUMBER -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, null)
                holder = ChildItemHolder(view)
                initNumberPickerDialog()
                return holder
            }
            TYPE_INTENT -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_of_settings_child, null)
                holder = ChildItemHolder(view)
                initIntentItem()

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

    private fun getPrefill() :String?{
        val d = settingItem.defaultValue.invoke() as String?
        var prefill: String? = d
        if (settingItem.keyId == null) {
            prefill = d
        } else {
            val key = settingItem.key?:return d

            if(key in AppConfig.settings) {
                AppConfig.settings.getString(key).also {
                    if (it != "") settingItem.summary = it
                    prefill = it
                }
            }
        }
        return prefill
    }
    private fun initAndSetInputListener() {
        val backSummary: String? = settingItem.summary

        //初始化summary
        getPrefill()

        setBasic {
            val prefill = getPrefill()
            MaterialDialog(context).title(text = settingItem.title()).input(prefill = prefill) { d, c ->
                Vog.d("initAndSetInputListener ---> $c")
                val s = c.toString()
                settingItem.summary = s
                if ((settingItem.callback as CallbackOnSet<String>?)?.invoke(ItemOperation(this), s) != false) {
                    settingItem.keyId?.also {
                        AppConfig.set(settingItem.keyId, s)
                    }
                }
                setBasic()
            }.show {
                positiveButton()
                neutralButton(text = "清空") {
                    if (settingItem.keyId != null) {
                        AppConfig.set(settingItem.keyId, null)
                    }
                    settingItem.summary = backSummary
                    setBasic()
                }
                negativeButton()
            }
        }
    }

    /**
     * 通知AppConfig 加载不规则类型
     */
    private fun loadConfigInCacheThread() {
        ThreadPool.runOnCachePool {
            AppConfig.reload()
        }
    }

    /**
     * @param lis View.OnClickListener
     */
    fun setBasic(lis: OnClick? = null) {
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

        if (item.keyId != null) {
            val sp = SpHelper(context)
            val b = sp.getBoolean(item.keyId, item.defaultValue.invoke() as Boolean)
            holder.compoundWight.isChecked = b
            holder.compoundWight.setOnCheckedChangeListener { _, isChecked ->
                if ((item.callback as CallbackOnSet<Boolean>?)?.invoke(ItemOperation(this), isChecked) != false) {
                    AppConfig.set(item.keyId, isChecked)
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
    private fun getInitPos(): Int {
        val item = settingItem
        val default = item.defaultValue.invoke() as Int? ?: -1
        if(default>=0) {
            item.summary = item.items?.get(default)
        }
        val key = item.key
        key ?: return default
        if (AppConfig.settings.contains(key)) {
            val entity = context.resources.getStringArray(item.entityArrId!!)
            return try {
                val v = AppConfig.settings.getString(key)
                item.summary = v
                entity.indexOf(v)
            } catch (e: Exception) {//保存值为int
                val index = AppConfig.settings.getInt(key, 0)
                item.summary = entity[index]
                index
            }
        }
        return default
    }

    /**
     * 初始化单选对话框
     */
    private fun initSingleDialog() {
        val item = settingItem

        val items =
            if (item.keyId != null) {
                if (item.entityArrId != null)
                    context.resources.getStringArray(item.entityArrId).asList()
                else item.items!!
            } else item.items!!

        items.getOrNull(getInitPos())?.also {
            item.summary = it
        }
        setBasic {
            val init = getInitPos()
            MaterialDialog(context)
                    .title(text = item.title())
                    .listItemsSingleChoice(items = items, initialSelection = init) { _, i, t ->
                        //选择
                        if(i == init) return@listItemsSingleChoice

                        if ((item.callback as CallbackOnSet<Pair<Int, String>>?)?.invoke(ItemOperation(this), Pair(i, t)) != false) {
                            if (item.keyId != null) {
                                AppConfig.set(item.keyId, t)
                                loadConfigInCacheThread()
                            }
                            item.summary = t
                            setBasic()
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
    private fun initMultiDialog() {
        val item = settingItem
        val sp = SpHelper(context)
//        val entity = context.resources.getStringArray(item.entityArrId!!)
//
//        val v = if (item.keyId != null) sp.getString(item.keyId) else item.defaultValue.invoke()

        setBasic()

        MaterialDialog(context).title(text = item.title())
                .listItemsMultiChoice(item.entityArrId) { _, _, ts ->
                    if ((item.callback as CallbackOnSet<List<String>>?)?.invoke(ItemOperation(this), ts) != false) {
                        if (item.keyId != null) {
                            sp.set(item.keyId, ts)
                            loadConfigInCacheThread()
                        }
                        item.summary = ts.toString()
                        setBasic()
                    }
                    // callback
                }.show()
    }

    /**
     * 初始化 数字选择器
     */
    private fun initNumberPickerDialog() {
        val item = settingItem
        val sp = SpHelper(context)
        var old = if (item.keyId == null) item.defaultValue.invoke() as Int
        else sp.getInt(item.keyId)
        if (old == -1) {
            old = item.defaultValue.invoke() as Int
            item.summary = item.summary ?: old.toString()
        } else
            item.summary = old.toString()

        setBasic {
            val vv = buildNumberPickerView(item.range!!, old)
            MaterialDialog(context).title(text = item.title())
                    .customView(null, vv.first)
                    .positiveButton {
                        if ((item.callback as CallbackOnSet<Int>?)?.invoke(ItemOperation(this), old) != false) {
                            item.summary = old.toString()
                            if (item.keyId != null) {
                                AppConfig.set(item.keyId, old)
                            }
                            setBasic()
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