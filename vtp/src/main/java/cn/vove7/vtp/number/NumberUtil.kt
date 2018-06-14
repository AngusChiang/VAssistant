package cn.vove7.vtp.number

import java.lang.Math.abs

/**
 *
 *
 * Created by Vove on 2018/6/13
 */
object NumberUtil {

    private val unitArray = arrayOf("", "十", "百", "千")
    private val oArray = arrayOf("", "万", "亿")

    private val numZhLowerArray = arrayOf("零", "一", "二", "三", "四", "五", "六", "七", "八", "九")

    /**
     * 数字转中文大写
     * 范围: (Int.MIN_VALUE - Int.MAX_VALUE)
     * @see Int.MIN_VALUE
     * @see Int.MAX_VALUE
     * @param minusPerStr 负数前缀
     * @return Int -> 中文大写
     */
    fun num2Capital(ni: Int, minusPerStr: String = "负"): String {
        val isMinus = ni < 0
        val num = abs(ni)
        if (num < 10) {
            return numZhLowerArray[num]
        }
        val builder = StringBuilder()

        var s = num.toString()
        val headLen = s.length % 4

        var head = s.substring(0, headLen)
        if (headLen != 0) {
            if (head.isNotEmpty())
                builder.append(buildLess10Thousand(head.toInt())).append(oArray[s.length / 4])
            s = s.substring(headLen)
        }
        while (s.isNotEmpty()) {
            head = s.substring(0, 4)
            val n = head.toInt()
            s = s.substring(4)
            builder.append(if (n >= 1000 || n == 0) "" else numZhLowerArray[0])
                    .append(buildLess10Thousand(n))
                    .append(if (n == 0) "" else oArray[s.length / 4])
        }
        return (if (isMinus) minusPerStr else "") + builder.toString()
    }

    private fun buildLess10Thousand(num: Int): String {
        val s = num.toString().toList()
        val builder = StringBuilder()
        var len = s.size
        var tmp = ""

        for (i in s) {
            val n = i - '0'
            len--
            tmp = if (n == 0) {
                if (tmp != numZhLowerArray[0])
                    numZhLowerArray[0]
                else
                    continue
            } else {
                builder.append(tmp).append(
                        when {
                            len == 0 -> numZhLowerArray[n]
                            (s.size == 2 && n == 1) -> unitArray[len]
                            else -> "${numZhLowerArray[n]}${unitArray[len]}"
                        }
                )
                ""
            }
        }
        return builder.toString()
    }

}