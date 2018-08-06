package cn.vove7.common.view.finder

import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper

/**
 * # ViewFinderByText
 *
 * Created by Vove on 2018/7/7
 */
class ViewFinderByText : ViewFinder {
    constructor(accessibilityService: AccessibilityApi) : super(accessibilityService)
    constructor(accessibilityService: AccessibilityApi, text: String) : super(accessibilityService) {
        this.text = text
    }

    constructor(accessibilityService: AccessibilityApi, matchType: Int, text: String) : super(accessibilityService) {
        this.matchType = matchType
        this.text = text
    }

    var matchType = MATCH_MODE_EQUAL
    lateinit var text: String

    /**
     * 查找条件
     */
    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        Vog.v(this, "findCondition ${node.text} - $text")
        return when (matchType) {
            MATCH_MODE_EQUAL -> {
                node.text != null && node.text == text
            }
            MATCH_MODE_CONTAIN -> {
                node.text != null && node.text.contains(text)
            }
            MATCH_MODE_FUZZY_WITH_PINYIN -> {
                val f = TextHelper.compareSimilarityWithPinyin("${node.text}", text)
                Vog.d(this, "findCondition $f")
                return f >= 0.9
            }
            MATCH_MODE_FUZZY_WITHOUT_PINYIN -> {
                val f = TextHelper.compareSimilarity("${node.text}", text)
                Vog.d(this, "findCondition $f")
                return f >= 0.9
            }
            else -> {
                Vog.d(this, "findCondition 未知条件")
                false
            }
        }
    }

    companion object {
        const val MATCH_MODE_EQUAL = 427
        const val MATCH_MODE_CONTAIN = 426
        const val MATCH_MODE_FUZZY_WITH_PINYIN = 100
        const val MATCH_MODE_FUZZY_WITHOUT_PINYIN = 227
    }

}