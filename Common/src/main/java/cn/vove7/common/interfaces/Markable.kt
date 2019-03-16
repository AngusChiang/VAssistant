package cn.vove7.common.interfaces

import cn.vove7.common.datamanager.executor.entity.MarkedData

/**
 * # Markable
 *
 * @author 17719
 * 2018/8/9
 */
interface Markable {
    fun addMark(data: MarkedData)
}