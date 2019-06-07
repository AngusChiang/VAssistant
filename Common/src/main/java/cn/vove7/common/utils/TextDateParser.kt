package cn.vove7.common.utils

import java.util.*

/**
 * # TextDateParser
 *
 * @author Administrator
 * 2018/10/21
 */
object TextDateParser {

    /**
     * 解析中文时间
     * eg: arrayOf(
     * "十二点", "八点四十五", "八点半", "晚上八点", "中午12点", "下午2点一刻",
     * "明天中午", "后天下午3点", "大后天中午", "昨天下午2:21", "前天下午两点半",
     * "周一下午", "下周二八点半", "周日晚上八点",
     * "二十号晚上七点", "21号", "二十八号", "下个月十八号上午8点二十三", "十二月25号",
     * "12月8号上午8点", "周二一点", "这周五八点", "周五晚上7点半",
     * "一小时后", "一个半小时后", "半小时后", "两个半小时后", "45分钟后", "三十二分钟后",
     * "两个小时后", "两小时后", "二十小时后",
     * "八天后"
     * )
     * @param s String
     * @return Calendar? 解析失败返回空
     */
    fun parseDateText(s: String): Calendar? {
        var haveTime = false//文字包含日期时间信息
        val c = Calendar.getInstance().apply {
            //清空
            set(Calendar.MILLISECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.HOUR_OF_DAY, 8)//默认12:00
            val md = parseMonthAndDay(s)
            if (md != null) {
                haveTime = true
                set(Calendar.MONTH, md.first)
                set(Calendar.DAY_OF_MONTH, md.second)
            } else {
                parseOffsetDay(s)?.also {
                    haveTime = true
                    add(Calendar.DAY_OF_MONTH, it)
                }
            }
            parseHourAndTime(s).also { ht ->
                //时分
                if (ht != null) {
                    haveTime = true
                    val h = ht.first
                    //region 调整时间
//            if (h < 12) {
//                Calendar.getInstance().also { c ->
//                    if (get(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH) && get(Calendar.MONTH) == c.get(Calendar.MONTH)) {
//                        //当天
//                        val nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)//
//                        if (nowHour > h)
//                            h += 12
//                    }
//                }
//            }
                    //endregion a
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, ht.second)
                } else {//offset hour minute
                    val c = Calendar.getInstance()
                    val offHt = parseOffsetHourTime(s) //与parseHourAndTime分开好处理
                    if (offHt != null) {
                        haveTime = true
                        set(Calendar.HOUR_OF_DAY, offHt.first + c.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, offHt.second + c.get(Calendar.MINUTE))
                    } /*else {// 没指定时分默认当前时分
                        set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, c.get(Calendar.MINUTE))
                    }*/
                }
            }
        }
        return if (haveTime) c else null
    }

    /**
     * 解析月份日期
     * n月nn号 数字/数字
     * n号
     * @param s String
     * @return Pair<Int, Int>
     */
    private fun parseMonthAndDay(s: String): Pair<Int, Int>? {
        val thisMonth = Calendar.getInstance().get(Calendar.MONTH)
        matchValues(s, "%(下|这)个?月%号%").also {
            //解析 这|下 个? 月
            if (it != null) {
                return Pair(if (it[1] == "这") thisMonth
                else thisMonth + 1, toNum(it[2]))
            }
        }
        matchValues(s, "%#月%号%").also {
            // 识别结果 -> n/n  数字
            if (it != null) {
                try {
                    return Pair(toNum(it[1]) - 1, toNum(it[2]))
                } catch (e: Exception) {
                }
            }
        }
        matchValues(s, "%#号%").also {
            if (it != null) return Pair(thisMonth, toNum(it[1]))
        }
        return null
    }

    /**
     * 解析相对天数
     * %天
     * (星期|周)%
     * x天后
     * @param s String
     * @return Int
     */
    private fun parseOffsetDay(s: String): Int? {
        mapOf(
                Pair("明天", 1),
                Pair("明晚", 1),
                Pair("大后天", 3),//
                Pair("后天", 2),//
                Pair("今天", 0),
                Pair("昨天", -1),
                Pair("前天", -2)
        ).forEach {
            if (s.contains(it.key)) return it.value
        }
        val ss = matchValues(s, "%(星期|周)%")//这?周
        if (ss != null) {
            var week = hashMapOf(//解析周
                    Pair("一", 1), Pair("二", 2), Pair("三", 3),
                    Pair("四", 4), Pair("五", 5), Pair("六", 6),
                    Pair("日", 7), Pair("天", 7), Pair("末", 7)
            )[ss[2].substring(0, 1)]
            val dNextWeek = if (s.contains("下周") || s.contains("下星期")) 7 else 0

            val todayWeek = todayWeek
            if (week == null) week = todayWeek
            return if (week > todayWeek) { //星期大于今天
                week - todayWeek + dNextWeek
            } else {//1 3 小于
                7 + week - todayWeek
            }
        } else {//x天后
            matchValues(s, "%#天后%")?.also {
                return toNum(it[1])
            }
        }
        return null// default today
    }

    /**
     * 1 - 7 周一 - 周日
     */
    private val todayWeek: Int
        get() = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).let {
            if (it == Calendar.SUNDAY) 7
            else it - 1
        }

    /**
     * 大写数字转int
     * 八 -> 8
     * 十 -> 10
     * 二十 -> 20
     * 21 -> 21
     * @param s String
     */
    fun toNum(s: String): Int {
        return try {
            s.toInt()
        } catch (e: NumberFormatException) {
            parseChineseNumber(s).toInt()
        }
    }

    /**
     * 中文大写转int
     *
     * @param s String
     */
    fun parseChineseNumber(s: String): Long {
        val cUnit = hashMapOf(
                Pair('亿', 100000000L)
                , Pair('万', 10000L)
                , Pair('千', 1000L)
                , Pair('百', 100L)
                , Pair('十', 10L)
        )
        val stack = Stack<Long>()
        s.forEach { c ->
            val cu = cUnit[c]
            if (cu != null) {//单位
                stack.push(stack.poolS(cu) * cu)
            } else {//数字
                val n = c.toNumber()
                stack.push(n.toLong())
            }
        }
        return stack.sum()
    }

    /**
     * 获得时分
     * prefix: 早上(8:00)|上午|中午(12:00)|下午|晚上|凌晨
     * or x点|x点半|x点xx(分)?
     * 中午 默认12:00
     * 下午 默认14:00
     * 晚上 默认19:00
     * 凌晨 默认05:00
     *
     * @param s String
     * @return Pair<Int, Int> default:null
     */
    private fun parseHourAndTime(s: String): Pair<Int, Int>? {
        var hour = 12
        var minute = 0
        var haveResult = false
        matchValues(s, "%(周.)?#点半%").also {
            if (it != null) {
                hour = toNum(it[2])
                minute = 30
                haveResult = true
            }
        }
        if (!haveResult)
            matchValues(s, "%(周.)?#(点|:)#刻%").also {
                //七点x刻
                if (it != null) {
                    hour = toNum(it[2])
                    minute = toNum(it[4]) * 15
                    haveResult = true
                }
            }
        if (!haveResult)
            matchValues(s, "%(周.)?#(点|:)#%").also {
                //8:15 八点十五 6点25
                if (it != null) {
                    hour = toNum(it[2])
                    minute = try {
                        toNum(it[4])
                    } catch (e: Exception) {
                        0
                    }
                    haveResult = true
                }
            }
        return if (!haveResult) {
            when {
                s.contains("中午") -> Pair(12, 0)
                s.contains("下午") -> Pair(14, 0)
                s.contains("晚上") -> Pair(19, 0)
                s.contains("凌晨") -> Pair(5, 0)
                s.contains("这个时[候|间]".toRegex()) -> {
                    val p = Calendar.getInstance()
                    Pair(p.get(Calendar.HOUR_OF_DAY), p.get(Calendar.MINUTE))
                }
                else -> null//无结果 无(上下午)匹配
            }
        } else {
            if (s.contains("(下午|晚上|明晚)".toRegex())) {
                if (hour < 12) hour += 12
            }
            //else 早上|上午|中午|凌晨
            Pair(hour, minute)//default
        }
    }


    /**
     * 解析相对时分后
     * "一小时后", "半小时后", "45分钟后", "三十二分钟后", "两个小时后", "两小时后"
     * 一个?小时后
     * 半小时后
     * @param s String
     * @return Pair<Int, Int> offset 时 分
     */
    private fun parseOffsetHourTime(s: String): Pair<Int, Int>? {
        matchValues(s, "%#个?(半?)小时(后|候)%")?.also {
            val offsetM = if (it.contains("半")) 30 else 0
            return Pair(toNum(it[1]), offsetM)
        }
        matchValues(s, "%#个?小时#分钟(后|候)%")?.also {
            val offH = toNum(it[1])
            val offM = toNum(it[2])
            return Pair(offH, offM)
        }
        matchValues(s, "%#分钟(后|候)%")?.also {
            val offM = toNum(it[1])
            return Pair(0, offM)
        }
        return null
    }

    private val REG_ALL_CHAR = "([\\S\\s]*?)"
    val REG_NUMBER_CHAR = "([0-9零一二两三四五六七八九十百千万]*)"
    private fun dealRawReg(r: String): Regex = r.replace("%", REG_ALL_CHAR)
            .replace("#", REG_NUMBER_CHAR).toRegex()

    /**
     * 根据正则字符串，匹配出占位符对应的值
     * @param text String
     * @param regexStr String
     * @return Array<String>
     */
    private fun matchValues(text: String?, regexStr: String?): Array<String>? {
        if (text == null || regexStr == null) return null
        val reg = dealRawReg(regexStr)
        val r = reg.matchEntire(text)
        return r?.groupValues?.subList(1, r.groupValues.size)?.toTypedArray()
    }
}

//数字大写转小写
fun Char.toNumber(): Int {
    return if (this == '两') 2
    else arrayOf('零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十')
            .indexOf(this)
}

private fun Stack<Long>.poolS(l: Long): Long {
    if (isEmpty()) return 1
    var q: Long
    var sum = 0L
    while (isNotEmpty()) {
        if (peek() < l) {
            q = pop()
            sum += q
        } else break
    }
    return sum
}
