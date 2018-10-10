package cn.vove7.common.utils

/**
 * # ArrayUtil
 *
 * @author Administrator
 * 2018/10/9
 */
object ArrayUtil {

    fun merge(aaa: Array<Array<String>>): Array<String> {
        val list= mutableListOf<String>()
        aaa.forEach {
            list.addAll(it)
        }
        return list.toTypedArray()
    }
}