package cn.vove7.common.utils

import kotlin.reflect.KProperty

/**
 * 一个初始化值，在第一次获取后变为第二个值
 * @param T
 * @property initValue T
 * @property afterValue T
 * @property value T
 * @constructor
 */
class MutableFlag<T>(val initValue: T, val afterValue: T) {
    var value: T = initValue

    operator fun getValue(thisRef: Any?, p: KProperty<*>): T {
        val a = value
        value = afterValue
        return a
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, t: T) {
        value = t
    }
}