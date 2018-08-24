package cn.vove7.common.view.finder

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper

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
                        if (f > 0.8) {
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
                        if (f > 0.8) {
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
        return "ViewFinderWithMultiCondition(" +
                (if (viewText.isNotEmpty()) "viewText=$viewText" else "") +
                ", textMatchMode=$textMatchMode" +
                (if (viewId != null) ", viewId=$viewId" else "") +
                (if (desc.isNotEmpty()) ", desc=$desc" else "") +
                (if (editable == true) ", editable=$editable" else "") +
                (if (scrollable == true) ", scrollable=$scrollable)" else "")
    }

    companion object {
        const val MATCH_MODE_EQUAL = 427
        const val MATCH_MODE_CONTAIN = 426
        const val MATCH_MODE_FUZZY_WITH_PINYIN = 100
        const val MATCH_MODE_FUZZY_WITHOUT_PINYIN = 227
    }
}
