package cn.vove7.common.utils

import kotlin.reflect.KProperty

/**
 * 一个初始化值，在获取后变为第二个值
 * 固执
 * @param T
 * @property initValue T
 * @property afterValue T
 * @property value T
 * @constructor
 */
class StubbornFlag<T>(val initValue: T, val afterValue: T = initValue) {
    var value: T = initValue

    /**
     * get后恢复after值
     * @param thisRef Any?
     * @param p KProperty<*>
     * @return T
     */
    operator fun getValue(thisRef: Any?, p: KProperty<*>): T {
        val a = value
        value = afterValue
        return a
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, t: T) {
        value = t
    }
}