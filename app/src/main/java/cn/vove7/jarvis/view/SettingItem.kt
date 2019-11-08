package cn.vove7.jarvis.view

import android.support.annotation.ArrayRes
import android.widget.CompoundButton
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.set
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
 * @property entityArrId Int?
 * @constructor
 */
open class SettingChildItem(
        val titleId: Int?,
        val title: String?,
        var summary: String? = null,
        val itemType: Int,
        val keyId: Int? = null,
        val defaultValue: (() -> Any?),
//        val autoSetValue: Boolean = keyId!=null,
        val range: Pair<Int, Int>? = null,
        val callback: CallbackOnSet<*>? = null,
        val entityArrId: Int? = null,
//        val valueArrId: Int? = null,
        val items: List<String>? = null,
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

    val choiceItems
        get() = entityArrId?.let { GlobalApp.APP.resources.getStringArray(it).toList() } ?: items!!
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


class NumberPickerItem : SettingChildItem, ItemDialogAction {
    val onChange: Function1<Int, Unit>?
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
    ) : super(titleId, title, summary, TYPE_NUMBER, keyId, defaultValue, range = range,
            callback = callback) {
        this.onChange = onChange
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
    ) : super(titleId, title, summary, TYPE_NUMBER, keyId, defaultValue, range = range.first to range.last,
            callback = callback) {
        this.onChange = onChange
        this.onDialogDismiss = onDialogDismiss
    }
}

class SingleChoiceItem(
        titleId: Int? = null,
        title: String? = null,
        summary: String? = null,
        keyId: Int? = null,
        defaultValue: Int = -1,//pos
        @ArrayRes entityArrId: Int? = null,
        items: List<String>? = null,
        allowClear: Boolean = false,
        callback: CallbackOnSet<Pair<Int, String>?>? = null
) : SettingChildItem(titleId, title, summary, TYPE_SINGLE, keyId, { defaultValue },
        entityArrId = entityArrId, callback = callback, items = items, allowClear = allowClear)

val storeIndexOnSingleChoiceItem: CallbackOnSet<Pair<Int, String>> = { io, it ->
    io.keyId?.also { ki ->
        AppConfig.settings.set(ki, it.first)
        io.summary = it.second
    }
    false
}

class IntentItem(titleId: Int? = null,
                 title: String? = null,
                 summary: String? = null,
                 onClick: () -> Unit)
    : SettingChildItem(titleId, title, summary, itemType = TYPE_INTENT,
        defaultValue = { Any() }, callback = { _, _ ->
    onClick.invoke()
    true
})

class InputItem(titleId: Int? = null,
                title: String? = null,
                summary: String? = null,
                keyId: Int? = null,
                defaultValue: () -> String = { "" },
                callback: CallbackOnSet<String>? = null)
    : SettingChildItem(titleId, title, summary, itemType = TYPE_INPUT, keyId = keyId,
        defaultValue = defaultValue, callback = callback)