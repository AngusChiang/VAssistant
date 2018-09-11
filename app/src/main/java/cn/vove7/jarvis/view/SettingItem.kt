package cn.vove7.jarvis.view

import android.support.annotation.ArrayRes
import cn.vove7.jarvis.view.utils.SettingItemHelper

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
const val TYPE_MULTI = 5
const val TYPE_NUMBER = 6
const val TYPE_SWITCH_CALLBACK = 8
const val TYPE_INTENT = 9

typealias CallbackOnSet = (SettingItemHelper.ChildItemHolder, Any) -> Unit

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
 * @property valueArrId Int?
 * @constructor
 */
open class SettingChildItem(
        val titleId: Int,
        var summary: String? = null,
        val itemType: Int,
        val keyId: Int? = null,
        val defaultValue: () -> Any,

        val range: Pair<Int, Int>? = null,
        val callback: CallbackOnSet? = null,
        val entityArrId: Int? = null,
        val valueArrId: Int? = null
)

class CheckBoxItem(
        titleId: Int,
        summary: String? = null,
        keyId: Int,
        defaultValue: () -> Boolean
) : SettingChildItem(titleId, summary, TYPE_CHECK_BOX, keyId, defaultValue)

class SwitchItem(
        titleId: Int,
        summary: String? = null,
        keyId: Int,
        defaultValue: () -> Boolean,
        callback: CallbackOnSet? = null
) : SettingChildItem(titleId, summary, TYPE_SWITCH, keyId, defaultValue, callback = callback)

/**
 * 值无需保存，状态在运行时改变
 * @constructor
 */
class SwitchItemWithoutSp(
        titleId: Int,
        summary: String? = null,
        callback: CallbackOnSet,
        defaultValue: () -> Boolean
) : SettingChildItem(titleId, summary, TYPE_SWITCH_CALLBACK, defaultValue = defaultValue, callback = callback)

class NumberPickerItem(
        titleId: Int,
        summary: String? = null,
        keyId: Int,
        defaultValue: () -> Int,
        range: Pair<Int, Int>,
        callback: CallbackOnSet? = null
) : SettingChildItem(titleId, summary, TYPE_NUMBER, keyId, defaultValue, range = range, callback = callback)

class SingleChoiceItem(
        titleId: Int,
        summary: String? = null,
        keyId: Int,
        defaultValue: () -> String,
        @ArrayRes entityArrId: Int,
        @ArrayRes valueArrId: Int,
        callback: CallbackOnSet? = null
) : SettingChildItem(titleId, summary, TYPE_SINGLE, keyId, defaultValue,
        entityArrId = entityArrId, valueArrId = valueArrId, callback = callback)

class IntentItem(titleId: Int, summary: String? = null, onClick: CallbackOnSet)
    : SettingChildItem(titleId, summary, itemType = TYPE_INTENT, defaultValue = { Any() }, callback = onClick)