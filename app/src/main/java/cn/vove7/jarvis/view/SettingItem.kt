package cn.vove7.jarvis.view

import androidx.annotation.ArrayRes
import android.widget.CompoundButton
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.view.tools.SettingItemHelper

/**
 * # SettingItem
 *
 * @author 17719247306
 * 2018/9/10
 */

const val TYPE_CHECK_BOX = 1
const val TYPE_SWITCH = 2
const val TYPE_INPUT = 3
const val TYPE_SINGLE = 4
@Deprecated("unused")
const val TYPE_MULTI = 5
const val TYPE_NUMBER = 6
const val TYPE_COLOR = 7
const val TYPE_INTENT = 9

/**
 * 返回结果用于是否设置
 * 返回false 不进行自动处理
 */
typealias CallbackOnSet<T> = (ItemOperation, T) -> Boolean

/**
 * SettingChildItem 操作
 * @property summary String?
 * @property title String?
 * @property isChecked Boolean?
 * @property compoundWight CompoundButton?
 * @constructor
 */
class ItemOperation(val itemHelper: SettingItemHelper) {
    var summary: String? = null
        get() = itemHelper.holder.summaryView.text.toString()
        set(value) {
            itemHelper.holder.summaryView.text = value
            field = value
        }

    val keyId get() = itemHelper.settingItem.keyId

    var title: String? = null
        get() = itemHelper.holder.titleView.text.toString()
        set(value) {
            itemHelper.holder.titleView.text = value
            field = value
        }

    var isChecked: Boolean? = false
        get() = compoundWight?.isChecked
        set(value) {
            compoundWight?.isChecked = value ?: false
            field = value
        }


    private val compoundWight: CompoundButton?
        get() {
            val holder = itemHelper.holder
            return if (holder is SettingItemHelper.CompoundItemHolder)
                holder.compoundWight
            else null
        }

}


/**
 * 基类
 *
 * 标题 summary 其他为,,,
 * @property titleId Int
 * @property summary String?
 * @property itemType Int
 * @property keyId Int?
 * @property defaultValue Function0<Any>
 * @property range Pair<Int, Int>?
 * @property callback Function1<Any, Unit>?
 * @constructor
 */
open class SettingChildItem(
        private val titleId: Int?,
        val title: String?,
        var summary: CharSequence? = null,
        val itemType: Int,
        val keyId: Int? = null,
        val defaultValue: (() -> Any?),
        val callback: CallbackOnSet<*>? = null,
        val allowClear: Boolean = false
) {
    fun title(): String {
        if (titleId != null)
            return GlobalApp.getString(titleId)
        if (title != null)
            return title

        return ".."
    }

    val key: String? get() = keyId?.let { GlobalApp.getString(it) }

}

//val reloadConfig: CallbackOnSet = { _, _ ->
//    AppConfig.reload()
//}

open class CompoundItem(
        titleId: Int? = null,
        title: String? = null,
        summary: String? = null,
        keyId: Int? = null,
        defaultValue: Boolean = false,
        val onTileAreaClick: Function0<Unit>? = null,
        callback: CallbackOnSet<Boolean>? = null,
        val type: Int
) : SettingChildItem(titleId, title, summary, type, keyId, { defaultValue },
        callback = callback)


class CheckBoxItem(
        titleId: Int? = null,
        title: String? = null,
        summary: String? = null,
        keyId: Int? = null,
        defaultValue: Boolean = false,
        onTileAreaClick: Function0<Unit>? = null,
        callback: CallbackOnSet<Boolean>? = null
) : CompoundItem(titleId, title, summary, keyId, defaultValue, onTileAreaClick, callback = callback, type = TYPE_CHECK_BOX)

class SwitchItem(
        titleId: Int? = null,
        title: String? = null,
        summary: String? = null,
        keyId: Int? = null,
        defaultValue: Boolean,
        onTileAreaClick: Function0<Unit>? = null,
        callback: CallbackOnSet<Boolean>? = null
) : CompoundItem(titleId, title, summary, keyId, defaultValue, onTileAreaClick, callback, TYPE_SWITCH)

interface ItemDialogAction {
    val onDialogDismiss: Function0<Unit>?
}

interface ItemChangeListener<T> {
    val onChange: Function1<T, Unit>?
}

class NumberPickerItem : SettingChildItem, ItemDialogAction, ItemChangeListener<Int> {
    val range: Pair<Int, Int>
    override val onChange: Function1<Int, Unit>?
    override val onDialogDismiss: Function0<Unit>?

    constructor(
            titleId: Int? = null,
            title: String? = null,
            summary: String? = null,
            keyId: Int? = null,
            defaultValue: () -> Int,
            range: Pair<Int, Int>,
            onChange: Function1<Int, Unit>? = null,
            onDialogDismiss: Function0<Unit>? = null,
            callback: CallbackOnSet<Int>? = null
    ) : super(titleId, title, summary, TYPE_NUMBER, keyId, defaultValue,
            callback = callback) {
        this.onChange = onChange
        this.range = range
        this.onDialogDismiss = onDialogDismiss
    }

    constructor(
            titleId: Int? = null,
            title: String? = null,
            summary: String? = null,
            keyId: Int? = null,
            defaultValue: () -> Int,
            range: IntRange,
            onChange: Function1<Int, Unit>? = null,
            onDialogDismiss: Function0<Unit>? = null,
            callback: CallbackOnSet<Int>? = null
    ) : super(titleId, title, summary, TYPE_NUMBER, keyId, defaultValue, callback = callback) {
        this.onChange = onChange
        this.range = range.first to range.last
        this.onDialogDismiss = onDialogDismiss
    }
}

class ColorPickerItem(
        titleId: Int? = null,
        title: String? = null,
        keyId: Int? = null,
        defaultValue: Int,
        override val onChange: Function1<Int, Unit>? = null,
        override val onDialogDismiss: Function0<Unit>? = null,
        callback: CallbackOnSet<Int>? = null
) : SettingChildItem(titleId, title, null, TYPE_COLOR, keyId, { defaultValue }, callback = callback),
        ItemDialogAction, ItemChangeListener<Int>

class SingleChoiceItem(
        titleId: Int? = null,
        title: String? = null,
        summary: String? = null,
        keyId: Int? = null,
        defaultValue: Int = -1,//pos
        @ArrayRes val entityArrId: Int? = null,
        val items: List<String>? = null,
        allowClear: Boolean = false,
        callback: CallbackOnSet<Pair<Int, String>?>? = null
) : SettingChildItem(titleId, title, summary, TYPE_SINGLE, keyId, { defaultValue },
        callback = callback, allowClear = allowClear) {
    val choiceItems
        get() = entityArrId?.let {
            GlobalApp.APP.resources.getStringArray(it).toList()
        } ?: items!!
}

class IntentItem(titleId: Int? = null,
                 title: String? = null,
                 summary: String? = null,
                 onClick: (ItemOperation) -> Unit)
    : SettingChildItem(titleId, title, summary, itemType = TYPE_INTENT,
        defaultValue = { Any() }, callback = { i, _ ->
    onClick.invoke(i)
    true
})

class InputItem(titleId: Int? = null,
                title: String? = null,
                summary: String? = null,
                keyId: Int? = null,
                defaultValue: () -> String = { "" },
                val clearable:Boolean = true,
                callback: CallbackOnSet<String>? = null)
    : SettingChildItem(titleId, title, summary, itemType = TYPE_INPUT, keyId = keyId,
        defaultValue = defaultValue, callback = callback)