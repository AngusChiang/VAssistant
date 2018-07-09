package cn.vove7.jarvis.view.finder

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.text.TextHelper

/**
 * # ViewFinderByText
 *
 * Created by Vove on 2018/7/7
 */
class ViewFinderByText : ViewFinder {
    constructor(accessibilityService: AccessibilityService) : super(accessibilityService)
    constructor(accessibilityService: AccessibilityService, text: String) : super(accessibilityService) {
        this.text = text
    }

    constructor(accessibilityService: AccessibilityService, matchType: Int, text: String) : super(accessibilityService) {
        this.matchType = matchType
        this.text = text
    }

    var matchType = MATCH_TYPE_EQUAL
    lateinit var text: String

    /**
     * 查找条件
     */
    override fun findCondition(node: AccessibilityNodeInfo): Boolean {
        Vog.v(this, "findCondition ${node.text} - $text")
        return when (matchType) {

            MATCH_TYPE_EQUAL -> {
                node.text != null && node.text == text
            }
            MATCH_TYPE_FUZZY_WITH_PINYIN -> {
                val f= TextHelper.compareSimilarityWithPinyin("${node.text}", text)
                Vog.d(this,"findCondition $f")
                return f >= 0.9
            }
            MATCH_TYPE_FUZZY_WITHOUT_PINYIN -> {
                val f=TextHelper.compareSimilarity("${node.text}", text)
                Vog.d(this,"findCondition $f")
                return f >= 0.9
            }
            else -> {
                Vog.d(this, "findCondition 未知条件")
                false
            }
        }
    }

    companion object {
        const val MATCH_TYPE_EQUAL = 427
        const val MATCH_TYPE_FUZZY_WITH_PINYIN = 100
        const val MATCH_TYPE_FUZZY_WITHOUT_PINYIN = 227
    }

}