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

    var viewText: String? = null
    var textMatchMode: Int = 0
    var viewId: String? = null
    var desc: String? = null
    var editable: Boolean? = null
    var scrollable: Boolean? = null

    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        if (viewText != null) {
            when (textMatchMode) {
                MATCH_MODE_EQUAL -> {
                    val b = node.text != null && node.text == viewText
                    if (!b) return false
                }
                MATCH_MODE_CONTAIN -> {
                    val b = node.text != null && node.text.contains(viewText!!)
                    if (!b) return false
                }
                MATCH_MODE_FUZZY_WITH_PINYIN -> {
                    val f = TextHelper.compareSimilarityWithPinyin(accessibilityService, "${node.text}", viewText!!)
                    Vog.v(this, "findCondition $f")
                    if (f < 0.9)
                        return false
                }
                MATCH_MODE_FUZZY_WITHOUT_PINYIN -> {
                    val f = TextHelper.compareSimilarity("${node.text}", viewText!!)
                    Vog.v(this, "findCondition $f")
                    if (f < 0.8)
                        return false
                }
                else -> {
                    Vog.v(this, "findCondition 未知条件")
                    return false
                }
            }
        }
        //could not remove "$.." prevent cause null
        if (viewId != null
                && !"${node.viewIdResourceName}".endsWith("/$viewId"))// :id/view_id)
            return false

        if (desc != null && "${node.contentDescription}" != desc)
            return false

        if (scrollable != null && node.isScrollable != scrollable) {
            return false
        }

        if (editable != null && node.isEditable != editable) {
            return false
        }
        Vog.i(this,"findCondition  find it ")
        return true
    }

    companion object {
        const val MATCH_MODE_EQUAL = 427
        const val MATCH_MODE_CONTAIN = 426
        const val MATCH_MODE_FUZZY_WITH_PINYIN = 100
        const val MATCH_MODE_FUZZY_WITHOUT_PINYIN = 227
    }
}
