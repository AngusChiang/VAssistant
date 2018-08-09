package cn.vove7.executorengine.model

/**
 * # Markable
 *
 * @author 17719
 * 2018/8/9
 */
interface Markable<M> {
    fun addMark(data: M)
}