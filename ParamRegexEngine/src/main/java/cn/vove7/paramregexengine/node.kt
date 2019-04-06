package cn.vove7.paramregexengine

import java.util.*


abstract class RegNode {
    lateinit var regText: String

    var matchValue: String = ""

    var minMatchCount = 1
    var maxMatchCount = 1

    var preNode: RegNode? = null
    var nextNode: RegNode? = null

    /**
     * 返回下一个待匹配
     * @param index Int
     * @param s String
     * @return Int
     */
    fun buildMatchCount(index: Int, s: String): Int {
        if (index >= s.length) return index
        when (s[index]) {
            '*' -> {
                minMatchCount = 0
                maxMatchCount = Int.MAX_VALUE
            }
            '+' -> {
                minMatchCount = 1
                maxMatchCount = Int.MAX_VALUE
            }
            '?' -> {
                minMatchCount = 0
                maxMatchCount = 1
            }
            //下面可以适配 {1,4} 匹配数量
            '{' -> {
                val endIndex = s.indexOf('}', index + 1)
                if (endIndex < index) throw ParamRegexParseException("缺少} at $index")
                val text = s.substring(index + 1, endIndex)
                if (text.contains(',')) {//{1,2} / {1,}
                    val ss = text.split(',')
                    minMatchCount = ss[0].trim().toInt()
                    maxMatchCount = ss[1].trim().let {
                        if (it.isEmpty()) Int.MAX_VALUE
                        else it.toInt()
                    }
                } else {//{2}
                    minMatchCount = text.trim().toInt()
                    maxMatchCount = minMatchCount
                }
                return endIndex + 1
            }
            else -> {
                when (this::class.java.simpleName) {
                    ParamNode::class.java.simpleName -> {
                        minMatchCount = 0
                        maxMatchCount = Int.MAX_VALUE
                    }
                    else -> {
                        minMatchCount = 1
                        maxMatchCount = 1
                    }
                }
                return index
            } // 匹配一次
        }
        return index + 1
    }


    /**
     * 向右匹配，返回结束下标
     * @param s String
     * @param startIndex Int
     * @param nextNode  用于预匹配
     * @return Int? 结束下标
     */
    abstract fun match(s: String, startIndex: Int, nextNode: RegNode?): Int?
}

class ParamNode : RegNode() {
    var name: String? = null
    var onlyNumber: Boolean = false

    val numValue get() = toNum(matchValue).toString()

    override fun match(s: String, startIndex: Int, nextNode: RegNode?): Int? {
        return if (onlyNumber) matchNumber(s, startIndex)
        else matchString(s, startIndex, nextNode)

    }

    private fun matchNumber(s: String, startIndex: Int): Int? {
        val l = s.length
        val numArray = "0123456789零一二两三四五六七八九十百千万"
        var endIndex = startIndex
        while (endIndex < l) {
            if (s[endIndex] in numArray) {
                endIndex++
            } else break//失败
        }
        if (startIndex == endIndex) {
            throw Exception("未匹配到数字")
        }
        matchValue = s.substring(startIndex, endIndex)
        return endIndex
    }

    private fun toNum(s: String): Int {
        return try {
            s.toInt()
        } catch (e: NumberFormatException) {
            parseChineseNumber(s).toInt()
        }
    }

    private fun matchString(s: String, startIndex: Int, nextNode: RegNode?): Int? {
        if (nextNode == null) {
            matchValue = s.substring(startIndex)
            return s.length
        }
        //
        for (i in startIndex + minMatchCount until s.length) {
            //防止nextNode minMatchCount = 0
            var node = nextNode
            var nextIndex: Int? = i
            while (node != null) {
                if (node.minMatchCount == 0) {//检查未匹配
                    nextIndex = node.match(s, i, nextNode.nextNode)
                    if (nextIndex == i) //未匹配到
                        node = node.nextNode
                    else break//匹配到
                } else {
                    nextIndex = node.match(s, i, node.nextNode)
                    break
                }
            }

            if (nextIndex != null) {
                val matchCount = i - startIndex
                if (matchCount !in minMatchCount..maxMatchCount) {
                    matchValue = ""
                    Vog.d(" 范围匹配失败 --> $matchCount not in ($minMatchCount, $maxMatchCount)")
                    return null
                }
                matchValue = s.substring(startIndex, i)
                return i
            }

        }
        return null
    }

    /**
     * 中文大写转int
     *
     * @param s String
     */
    private fun parseChineseNumber(s: String): Long {
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
                val n = u2l(c)
                stack.push(n.toLong())
            }
        }
        var sum = 0L
        stack.forEach {
            sum += it
        }
        return sum
    }

    //数字大写转小写
    private fun u2l(s: Char): Int {
        return if (s == '两') 2
        else arrayOf('零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十')
                .indexOf(s)
    }

}


class OrNode : RegNode() {
    var orList = mutableListOf<RegNode>()

    override fun match(s: String, startIndex: Int, nextNode: RegNode?): Int? {
        orList.withIndex().forEach {
            val endIndex = it.value.match(s, startIndex, null)
            if (endIndex != null) {
                matchValue = s.substring(startIndex, endIndex)
                return endIndex
            }
        }
        return null
    }
}

//[a-z257]
class CharsNode : RegNode() {

    private val rangeList = mutableListOf<CharRange>()

    fun buildOneCharNodes(regex: String, index: Int): Int {
        var i = index
        while (i < regex.length) {
            when (regex[i]) {
                in 'a'..'z', in 'A'..'Z', in '0'..'9' -> {

                    if (regex[i + 1] == '-') {
                        val range = CharRange(regex[i++], regex[++i])
                        rangeList.add(range)
                    } else {
                        rangeList.add(CharRange(regex[i], regex[i]))
                    }
                }
                ']' -> {
                    return buildMatchCount(++i, regex)
                }
                else -> {
                    rangeList.add(CharRange(regex[i], regex[i]))
                }
            }
            i++
        }
        throw Exception("[] 不匹配 at $i")
    }

    override fun match(s: String, startIndex: Int, nextNode: RegNode?): Int? {
        if (startIndex >= s.length) {
            return if (minMatchCount == 0) startIndex
            else null
        }

        var endIndex = startIndex
        for (i in startIndex until s.length) {
            if (i - startIndex >= maxMatchCount) break//限制次数
            val c = s[i]
            var contain = false
            rangeList.forEach forRange@{
                if (it.contains(c)) {
                    contain = true
                    return@forRange
                }
            }
            if (contain)
                endIndex = i + 1
            else break //匹配结束
        }
        if (minMatchCount == 0 && startIndex == endIndex) {
            return startIndex
        }
        val matchCount = endIndex - startIndex
        if (matchCount < minMatchCount) {
            Vog.d(" 匹配次数 --> $matchCount < $minMatchCount")
            return null//匹配失败
        }
        matchValue = s.substring(startIndex, endIndex)
        return endIndex
    }
}

class TextNode(val text: String) : RegNode() {
    init {
        regText = text
    }

    override fun match(s: String, startIndex: Int, nextNode: RegNode?): Int? {
        if (startIndex >= s.length) {
            return if (minMatchCount == 0) startIndex
            else null
        }

        val b = s.substring(startIndex).startsWith(text)
        return when {
            b -> {
                matchValue = text
                startIndex + text.length
            }
            minMatchCount == 0 -> {
                matchValue = ""
                Vog.d(" 忽略 --> $regText")
                startIndex
            }
            else -> null
        }
    }
}

/**
 * 被() 包起来的
 * @property subNodeList LinkedList<RegNode>
 */
class GroupNode : RegNode() {
    //与的关系 (123[abc])*
    lateinit var subNodeList: List<RegNode>

    override fun match(s: String, startIndex: Int, nextNode: RegNode?): Int? {
        if (startIndex >= s.length && minMatchCount != 0) return null
        var endIndex = startIndex
        var matchCount = 0//匹配次数
        while (endIndex < s.length && matchCount < maxMatchCount) {
            var i = 0
            var result = true
            subNodeList.forEach {
                //并列关系
                val partEndIndex = it.match(s, endIndex, subNodeList.getOrNull(i++))
                if (partEndIndex == null) {
                    result = false
                    return@forEach //匹配成功 下次匹配
                } else {//subList 匹配失败
                    //nextNode
                    endIndex = partEndIndex
                }
            }
            //一轮结束失败 && minMatchCount == 0
            if (!result && minMatchCount == 0) {
                Vog.d("group未匹配到")
                return startIndex //group未匹配到
            }
            if (result) {//匹配成功，继续向后匹配
                matchCount++
                continue
            } else {//第2+轮 失败
                return if (matchCount >= minMatchCount) {
                    matchValue = s.substring(startIndex, endIndex)
                    endIndex
                } else {
                    null
                }
            }
        }
        matchValue = s.substring(startIndex, endIndex)
//        Vog.d(" 匹配次数 --> $matchCount")
        return endIndex
    }
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
