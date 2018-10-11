package cn.vove7.common.view.finder

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
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

    var viewText: MutableList<String> = mutableListOf()
    var textMatchMode: Int = 0
    var viewId: String? = null
    var desc: MutableList<String> = mutableListOf()
    var editable: Boolean? = null
    var scrollable: Boolean? = null
    var typeNames: MutableList<String> = mutableListOf()
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
     * 使用深度搜索  TODO api
     * @param depths Array<Int>
     * @return ViewNode?
     */
    fun findByDepths(): ViewNode? {
        var p = accessibilityService.rootInActiveWindow
        depths.forEach {
            try {
                p = p.getChild(it)
            } catch (e: ArrayIndexOutOfBoundsException) {
                Vog.d(this, "findByDepths ---> ArrayIndexOutOfBounds null")
                return null
            }
            if (p == null) {
                return null
            }
        }
        return if (typeNames.isNotEmpty()) {
            if ("${p.className}".contains(typeNames[0], ignoreCase = true))
                ViewNode(p)
            else {
                Vog.e(this, "findByDepths ---> typeNames not match")
                null
            }
        } else ViewNode(p)
    }

    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        if (viewText.isNotEmpty()) {
            var ok = false
            when (textMatchMode) {
                MATCH_MODE_EQUAL -> {
                    for (it in viewText) {
                        val b = node.text != null && "${node.text}".equals(it, ignoreCase = true)
                        if (b) {
                            ok = true
                            break
                        }
                    }
                    if (!ok) return false
                }
                MATCH_MODE_CONTAIN -> {
                    for (it in viewText) {
                        val b = node.text != null && node.text.contains(it, ignoreCase = true)
                        if (b) {
                            ok = true
                            break
                        }
                    }
                    if (!ok) return false
                }
                MATCH_MODE_FUZZY_WITH_PINYIN -> {
                    for (it in viewText) {
                        val f = TextHelper.compareSimilarityWithPinyin(accessibilityService,
                                "${node.text}", it)
                        Vog.v(this, "findCondition $f")
                        if (f > 0.75) {
                            Vog.d(this, "find WITH_PINYIN $it")
                            ok = true
                            break
                        }
                    }
                    if (!ok) return false
                }
                MATCH_MODE_MATCHES -> {
                    for (it in viewText) {
                        if (dealRawReg(it).matches("${node.text}")) {
                            Vog.d(this, "find MATCHS $it")
                            ok = true
                            break
                        }
                    }
                    if (!ok) return false
                }
                MATCH_MODE_FUZZY_WITHOUT_PINYIN -> {
                    for (it in viewText) {

                        val f = TextHelper.compareSimilarity("${node.text}", it)
                        Vog.v(this, "findCondition $f")
                        if (f > 0.75) {
                            ok = true
                            break
                        }
                    }
                    if (!ok) return false
                }
                else -> {
                    Vog.v(this, "findCondition equal text 未知条件")
                    return false
                }
            }

        }
        //could not remove "$.." prevent cause null
        if (viewId != null
                && !"${node.viewIdResourceName}".endsWith("/$viewId"))// :id/view_id)
            return false

        if (desc.isNotEmpty()) {
            var ok = false
            for (it in desc) {
                val v = "${node.contentDescription}".equals(it, ignoreCase = true)
                if (v) {
                    ok = true
                    break
                }
            }
            if (!ok) return false
        }

        if (typeNames.isNotEmpty()) {
            var ok = false
            for (it in typeNames) {
                val v = "${node.className}".contains(it, ignoreCase = true)
                if (v) {
                    ok = true
                    Vog.d(this, "findCondition --->className contains $it")
                    break
                }
            }
            if (!ok) return false
        }

        if (scrollable != null && node.isScrollable != scrollable) {
            return false
        }

        if (editable != null && node.isEditable != editable) {
            return false
        }
        Vog.i(this, "findCondition  find it ")
        return true
    }

    override fun toString(): String {
        return "Condition(" +
                (if (viewText.isNotEmpty()) "viewText=$viewText" else "") +
                ", textMatchMode=$textMatchMode" +
                (if (viewId != null) ", viewId=$viewId" else "") +
                (if (desc.isNotEmpty()) ", desc=$desc" else "") +
                (if (typeNames.isNotEmpty()) ", typeNames=$typeNames" else "") +
                (if (depths.isNotEmpty()) ", depths=${Arrays.toString(depths)}" else "") +
                (if (editable == true) ", editable=$editable" else "") +
                (if (scrollable == true) ", scrollable=$scrollable)" else "") + ")"
    }

    companion object {
        const val MATCH_MODE_EQUAL = 427
        const val MATCH_MODE_MATCHES = 527
        const val MATCH_MODE_CONTAIN = 426
        const val MATCH_MODE_FUZZY_WITH_PINYIN = 100
        const val MATCH_MODE_FUZZY_WITHOUT_PINYIN = 227


        const val BY_PROPERTY = 0
        const val BY_DEPTHS = 1
    }
}
