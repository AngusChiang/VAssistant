package cn.vove7.common.view.finder

import android.util.Range
import android.view.accessibility.AccessibilityNodeInfo
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.view.finder.ViewFinderWithMultiCondition.Companion.TEXT_MATCH_MODE_CONTAIN
import cn.vove7.common.view.finder.ViewFinderWithMultiCondition.Companion.TEXT_MATCH_MODE_EQUAL
import cn.vove7.common.view.finder.ViewFinderWithMultiCondition.Companion.TEXT_MATCH_MODE_FUZZY_WITH_PINYIN
import cn.vove7.common.view.finder.ViewFinderWithMultiCondition.Companion.TEXT_MATCH_MODE_MATCHES
import java.util.*


/**
 * @author Vove
 *
 * 视图节点查找类
 *
 * 2018/8/5
 */
class ViewFindBuilder : FindBuilderWithOperation {

    private val viewFinderX: ViewFinderWithMultiCondition
        get() = finder as ViewFinderWithMultiCondition

    constructor() {
        finder = ViewFinderWithMultiCondition()
    }

    constructor(startNode: AccessibilityNodeInfo) {
        finder = ViewFinderWithMultiCondition(startNode)
    }

    override fun waitFor(): ViewNode? {
        return waitFor(30000L)
    }

    /**
     * 需要CExecutorI
     *
     * @param m 时限
     * @return ViewNode which is returned until show in screen
     */
    override fun waitFor(m: Long): ViewNode? {
        return viewFinderX.waitFor(m)
    }

    fun depths(ds: Array<Int>): ViewFindBuilder {
        viewFinderX.depths = ds
        return this
    }

    /**
     * 包含文本
     *
     * @param text text
     * @return this
     */
    fun containsText(vararg text: String): ViewFindBuilder {
        viewFinderX.addViewTextCondition(*text)
        viewFinderX.textMatchMode = TEXT_MATCH_MODE_CONTAIN
        return this
    }

    fun containsText(text: String): ViewFindBuilder {
        viewFinderX.addViewTextCondition(text)
        viewFinderX.textMatchMode = TEXT_MATCH_MODE_CONTAIN
        return this
    }

    /**
     * 正则匹配
     *
     * @param regs 表达式 %消息%
     * @return this
     */
    @Suppress(names = ["unused"])
    fun matchesText(vararg regs: String): ViewFindBuilder {
        viewFinderX.addViewTextCondition(*regs)
        viewFinderX.textMatchMode = TEXT_MATCH_MODE_MATCHES
        return this
    }

    @Suppress(names = ["unused"])
    fun matchesText(regs: String): ViewFindBuilder {
        viewFinderX.addViewTextCondition(regs)
        viewFinderX.textMatchMode = TEXT_MATCH_MODE_MATCHES
        return this
    }


    /**
     * 相同文本 不区分大小写
     *
     * @param text text
     * @return this
     */
    fun equalsText(vararg text: String): ViewFindBuilder {
        viewFinderX.addViewTextCondition(*text)
        viewFinderX.textMatchMode = TEXT_MATCH_MODE_EQUAL
        return this
    }

    fun equalsText(text: String): ViewFindBuilder {
        viewFinderX.addViewTextCondition(text)
        viewFinderX.textMatchMode = TEXT_MATCH_MODE_EQUAL
        return this
    }

    /**
     * 文本拼音相似度
     *
     * @param text 文本内容
     * @return this
     */
    @Suppress(names = ["unused"])
    fun similaryText(vararg text: String): ViewFindBuilder {
        viewFinderX.addViewTextCondition(*text)
        viewFinderX.textMatchMode = TEXT_MATCH_MODE_FUZZY_WITH_PINYIN
        return this
    }

    fun similaryText(text: String): ViewFindBuilder {
        viewFinderX.addViewTextCondition(text)
        viewFinderX.textMatchMode = TEXT_MATCH_MODE_FUZZY_WITH_PINYIN
        return this
    }

    fun textLengthLimit(limit: Int): ViewFindBuilder {
        viewFinderX.textLengthLimit = Range.create(0, limit)
        return this
    }

    @Suppress(names = ["unused"])
    fun textLengthLimit(lower: Int, upper: Int): ViewFindBuilder {
        viewFinderX.textLengthLimit = Range.create(lower, upper)
        return this
    }


    /**
     * 根据id 查找
     *
     * @param id viewId
     * @return
     */
    fun id(id: String): ViewFindBuilder {
        viewFinderX.viewId = id
        return this
    }

    /**
     * 说明
     *
     * @param desc
     * @return
     */
    fun desc(vararg desc: String): ViewFindBuilder {
        viewFinderX.descTexts.addAll(Arrays.asList(*desc))
        viewFinderX.descMatchMode = TEXT_MATCH_MODE_EQUAL
        return this
    }

    fun desc(desc: String): ViewFindBuilder {
        viewFinderX.descTexts.add(desc)
        viewFinderX.descMatchMode = TEXT_MATCH_MODE_EQUAL
        return this
    }

    fun containsDesc(desc: String): ViewFindBuilder {
        viewFinderX.descTexts.add(desc)
        viewFinderX.descMatchMode = TEXT_MATCH_MODE_CONTAIN
        return this
    }

    fun containsDesc(vararg desc: String): ViewFindBuilder {
        viewFinderX.descTexts.addAll(Arrays.asList(*desc))
        viewFinderX.descMatchMode = TEXT_MATCH_MODE_CONTAIN
        return this
    }

    @JvmOverloads
    fun editable(b: Boolean = true): ViewFindBuilder {
        viewFinderX.editable = b
        return this
    }

    @JvmOverloads
    fun scrollable(b: Boolean = true): ViewFindBuilder {
        viewFinderX.scrollable = b
        return this
    }

    fun type(type: String): ViewFindBuilder {
        viewFinderX.typeNames.add(type)
        return this
    }

    fun type(vararg types: String): ViewFindBuilder {
        viewFinderX.typeNames.addAll(Arrays.asList(*types))
        return this
    }

    fun await(): ViewNode? {
        return waitFor()
    }

    fun await(l: Long): ViewNode? {
        return waitFor(l)
    }

}
