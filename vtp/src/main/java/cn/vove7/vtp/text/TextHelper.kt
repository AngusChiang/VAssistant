package cn.vove7.vtp.text

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination


/**
 *
 *
 * Created by Vove on 2018/6/19
 */
object TextHelper {

    /**
     * 汉字转拼音
     * 一字多音？😔
     */
    fun chineseStr2Pinyin(words: String, onlyFirst: Boolean = false): String {
        var pinyinStr = ""
        val newChar = words.toCharArray()
        val defaultFormat = HanyuPinyinOutputFormat()
        defaultFormat.caseType = HanyuPinyinCaseType.LOWERCASE
        defaultFormat.toneType = HanyuPinyinToneType.WITHOUT_TONE
        for (c in newChar) {
            pinyinStr += if (c.toInt() > 128) {
                try {
                    val all = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat)[0]
                    if (onlyFirst) "${all[0]}" else all
                } catch (e: BadHanyuPinyinOutputFormatCombination) {
                    println(e.message ?: e.toString())
                    c
                } catch (ne: NullPointerException) {
                    println(ne.message ?: ne.toString())
                    c
                }
            } else {
                c
            }
        }
        return pinyinStr
    }

    /**
     * 汉字转拼音再比较相似度
     */

    fun compareSimilarityWithPinyin(str1: String, str2: String, ignoreCase: Boolean = true): Float {
        return compareSimilarity(chineseStr2Pinyin(str1), chineseStr2Pinyin(str2), ignoreCase)
    }

    /**
     * 比较相似度
     * Levenshtein Distance 算法实现
     * DNA分析 拼字检查 语音辨识 抄袭侦测
     *
     * [link](https://blog.csdn.net/basycia/article/details/51884350)
     */
    fun compareSimilarity(str1: String, str2: String, ignoreCase: Boolean = true): Float {
        var s1 = str1
        var s2 = str2
        if (ignoreCase) {
            s1 = str1.toLowerCase()
            s2 = str2.toLowerCase()
        }
        //计算两个字符串的长度
        val len1 = s1.length
        val len2 = s2.length
        //建立上面说的数组，比字符长度大一个空间
        val dif = Array(len1 + 1) { IntArray(len2 + 1) }
        //赋初值，步骤B
        for (a in 0..len1) {
            dif[a][0] = a
        }
        for (a in 0..len2) {
            dif[0][a] = a
        }
        //计算两个字符是否一样，计算左上的值
        var temp: Int
        for (i in 1..len1) {
            for (j in 1..len2) {
                temp = if (s1[i - 1] == s2[j - 1]) {
                    0
                } else {
                    1
                }
                //取三个值中最小的
                dif[i][j] = min(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1,
                        dif[i - 1][j] + 1)
            }
        }
//        println("字符串\"$s1\"与\"$s2\"的比较")
        //取数组右下角的值，同样不同位置代表不同字符串的比较
//        println("差异步骤：" + dif[len1][len2])
        //计算相似度
        //        println("相似度：$similarity")
        return 1 - dif[len1][len2].toFloat() / Math.max(s1.length, s2.length)
    }

    //得到最小值
    private fun min(vararg vars: Int): Int {
        var min = Integer.MAX_VALUE
        for (i in vars) {
            if (min > i) {
                min = i
            }
        }
        return min
    }


}