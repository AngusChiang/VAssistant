package cn.vove7.vtp.array

/**
 *
 *
 * Created by Vove on 2018/6/13
 */
object ArrayUtil {
    fun strArr2IntArr(sArr: Array<String>): List<Int> {
        val izc = arrayListOf<Int>()
        var index = 0
        for (s in sArr) {
            try {
                izc.add(s.toInt())
            } catch (e: Exception) {
                izc.add(0)
                e.printStackTrace()
            } finally {
                index++
            }
        }
        return izc
    }

}