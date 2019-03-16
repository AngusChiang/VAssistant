package cn.vove7.common.view.finder

import android.util.Range
import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.RegUtils.dealRawReg
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper
import java.util.*

/**
 * # ViewFinderWithMultiCondition
 * 多条件查询
 * @author 17719
 * 2018/8/5
 */
class ViewFinderWithMultiCondition(accessibilityService: AccessibilityApi) : ViewFinder(accessibilityService) {

    var viewTextCondition: MutableList<String> = mutableListOf()
    fun addViewTextCondition(vararg s: String) {
        viewTextCondition.addAll(s)
    }

    var textMatchMode: Int = 0
    var descMatchMode: Int = 0
    var viewId: String? = null
    var descTexts: MutableList<String> = mutableListOf()
    var editable: Boolean? = null
    var scrollable: Boolean? = null
    var typeNames: MutableList<String> = mutableListOf()
    var textLengthLimit: Range<Int>? = null
    var depths: Array<Int> = arrayOf()
        set(value) {
            findBy = BY_DEPTHS
            field = value
        }

    //    var extraParams: Array<String> = arrayOf()
    var findBy: Int = BY_PROPERTY

    override fun findFirst(): ViewNode? {
        return when (findBy) {
            BY_DEPTHS -> {
                findByDepths()
            }
            //            BY_PROPERTY
            else -> {
                super.findFirst()
            }
        }
    }

    /**
     * 使用深度搜索
     * @param depths Array<Int>
     * @return ViewNode?
     */
    fun findByDepths(): ViewNode? {
        var p: AccessibilityNodeInfo? = rootNode ?: {
            Vog.d("findByDepths ---> rootNode is null")
            null
        }.invoke()
        depths.forEach {
            try {
                p = p?.getChild(it)
            } catch (e: ArrayIndexOutOfBoundsException) {
                Vog.e("findByDepths ---> ArrayIndexOutOfBounds null")
                return null
            }
            if (p == null) {
                Vog.v("findByDepths ---> 搜索失败1")
                return null
            }
        }
        if (p == null) {
            Vog.v("findByDepths ---> 搜索失败2")
            return null
        }

        val pp = p!!
        return if (typeNames.isNotEmpty()) {
            if ("${pp.className}".contains(typeNames[0], ignoreCase = true))
                ViewNode(pp)
            else {
                Vog.e("findByDepths ---> typeNames not match")
                null
            }
        } else ViewNode(pp)
    }

    /**
     * 按查找条件查询
     * id text desc type
     * scrollable editable
     * @param node AccessibilityNodeInfo
     * @return Boolean
     */
    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        //could not remove "$.." prevent cause null
        val vid = "${node.viewIdResourceName}"
        if (viewId != null) {
            if (!vid.endsWith("/$viewId") && vid != viewId)
            // :id/view_id) //网页视图 id : id
                return false
        }

        val viewText = node.text
        //文字长度
        val tl = textLengthLimit
        if (tl != null && viewText != null && !tl.contains(viewText.length)) {
            return false
        }
        //匹配文字
        if (!matchTextWithCondition(textMatchMode, "${node.text}", viewTextCondition)) {
            return false
        }
        //匹配说明
        if (!matchTextWithCondition(descMatchMode, "${node.contentDescription}", descTexts)) {
            return false
        }
        //匹配className
        if (typeNames.isNotEmpty()) {
            var ok = false
            for (it in typeNames) {
                val v = "${node.className}".contains(it, ignoreCase = true)
                if (v) {
                    ok = true
                    Vog.d("findCondition --->className contains $it")
                    break
                }
            }
            if (!ok) return false
        }
        //可滑动
        if (scrollable != null && node.isScrollable != scrollable) {
            return false
        }
        //可编辑
        if (editable != null && node.isEditable != editable) {
            return false
        }
        Vog.i("findCondition find it ")
        return true
    }

    override fun toString(): String {
        return "Condition(" +
                (if (viewTextCondition.isNotEmpty()) "viewTextCondition=$viewTextCondition" else "") +
                ", textMatchMode=$textMatchMode" +
                (if (viewId != null) ", viewId=$viewId" else "") +
                (if (descTexts.isNotEmpty()) ", desc=$descTexts" else "") +
                (if (typeNames.isNotEmpty()) ", typeNames=$typeNames" else "") +
                (if (depths.isNotEmpty()) ", depths=${Arrays.toString(depths)}" else "") +
                (if (editable == true) ", editable=$editable" else "") +
                (if (scrollable == true) ", scrollable=$scrollable)" else "") + ")"
    }

    companion object {
        const val TEXT_MATCH_MODE_EQUAL = 427
        const val TEXT_MATCH_MODE_MATCHES = 527
        const val TEXT_MATCH_MODE_CONTAIN = 426
        const val TEXT_MATCH_MODE_FUZZY_WITH_PINYIN = 100
        const val TEXT_MATCH_MODE_FUZZY_WITHOUT_PINYIN = 227


        const val BY_PROPERTY = 0
        const val BY_DEPTHS = 1
        /**
         * 根据规则匹配文本
         * @param type Int
         * @param text String?
         * @param ms List<String>
         * @return Boolean true 继续  false 匹配失败
         */
        private fun matchTextWithCondition(type: Int, text: String?, ms: List<String>): Boolean {
            if (ms.isNotEmpty()) {
                var ok = false
                when (type) {
                    TEXT_MATCH_MODE_EQUAL -> {
                        for (it in ms) {
                            val b = it.equals(text, ignoreCase = true)
                            if (b) {
                                ok = true
                                break
                            }
                        }
                        if (!ok) return false
                    }
                    TEXT_MATCH_MODE_CONTAIN -> {
                        for (it in ms) {
                            val b = text != null && text.contains(it, ignoreCase = true)
                            if (b) {
                                ok = true
                                break
                            }
                        }
                        if (!ok) return false
                    }
                    TEXT_MATCH_MODE_FUZZY_WITH_PINYIN -> {
                        for (it in ms) {
                            val f = TextHelper.compareSimilarityWithPinyin(GlobalApp.APP,
                                    "$text", it, replaceNumberWithPinyin = true)
                            Vog.v("findCondition $f")
                            if (f > 0.75) {
                                Vog.d("find WITH_PINYIN $it")
                                ok = true
                                break
                            }
                        }
                        if (!ok) return false
                    }
                    TEXT_MATCH_MODE_MATCHES -> {
                        for (it in ms) {
                            if (dealRawReg(it).matches("$text")) {
                                Vog.d("find MATCHS $it")
                                ok = true
                                break
                            }
                        }
                        if (!ok) return false
                    }
                    TEXT_MATCH_MODE_FUZZY_WITHOUT_PINYIN -> {
                        for (it in ms) {
                            val f = TextHelper.compareSimilarity("$text", it)
                            Vog.v("findCondition $f")
                            if (f >= 0.75) {
                                ok = true
                                break
                            }
                        }
                        if (!ok) return false
                    }
                    else -> {
                        Vog.v("findCondition equal text 未知条件")
                        return false
                    }
                }
            }
            //匹配到
            return true
        }

    }
}
