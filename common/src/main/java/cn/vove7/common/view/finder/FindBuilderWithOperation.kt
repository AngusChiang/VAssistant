package cn.vove7.common.view.finder

import android.graphics.Point
import android.graphics.Rect
import cn.vove7.common.MessageException
import cn.vove7.common.ViewNodeNotFoundException
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.accessibility.viewnode.ViewOperation
import cn.vove7.common.utils.whileWaitTime
import cn.vove7.vtp.log.Vog

/**
 * # FindBuilderWithOperation
 *
 * @author 17719
 * 2018/8/10
 */

abstract class FindBuilderWithOperation
    : ViewOperation {

    val accessibilityService: AccessibilityApi? = AccessibilityApi.accessibilityService
    lateinit var finder: ViewFinder

    /**
     * 找到第一个
     * @return ViewNode
     */
    fun findFirst(): ViewNode? {
        return finder.findFirst()
    }

    abstract fun waitFor(): ViewNode?

    abstract fun waitFor(m: Long): ViewNode?

    /**
     *
     * @return list
     */
    fun find(): Array<ViewNode> {
        return finder.findAll()
    }

    /**
     * 默认10s等待时间
     * @return Boolean
     */
    fun waitHide(): Boolean {
        return waitHide(10000)
    }

    /**
     * 等待消失  常用于加载View的消失
     * @param waitMs max 60s
     * @return Boolean false 超时 true 消失
     */
    fun waitHide(waitMs: Int): Boolean {
        return whileWaitTime(waitMs.toLong()) {
            if (findFirst() != null) {
                Vog.d("未消失，等待")
                null
            }//显示，继续等待
            else {
                Vog.d("消失，停止等待")
                true
            } //消失
        } ?: false
    }

    fun waitHideUnsafely(waitMs: Int) {
        if (!waitHide(waitMs)) throw MessageException("视图未消失")
    }

    private val node: ViewNode
        get() = waitFor(WAIT_MILLIS) ?: throw ViewNodeNotFoundException(finder)

    override fun tryClick(): Boolean {
        return node.tryClick()
    }

    override fun click(): Boolean {
        return node.tryClick()
    }

    override fun globalClick(): Boolean {
        return node.globalClick()
    }

    override fun longClick(): Boolean {
        return node.longClick()
    }

    override fun doubleClick(): Boolean {
        return node.doubleClick()
    }

    override fun tryLongClick(): Boolean {
        return node.tryLongClick()
    }

    override fun getCenterPoint(): Point? {
        return node.getCenterPoint()
    }

    override fun select(): Boolean {
        return node.select()
    }

    override fun trySelect(): Boolean {
        return node.trySelect()
    }

    override fun scrollUp(): Boolean {
        return node.scrollUp()
    }

    override fun scrollDown(): Boolean {
        return node.scrollDown()
    }

    override fun setText(text: String, ep: String?): Boolean {
        return node.setText(text, ep)
    }

    override fun appendText(s: String) {
        node.appendText(s)
    }

    override fun desc(): String? {
        return node.desc()
    }

    override fun setTextWithInitial(text: String): Boolean {
        return node.setTextWithInitial(text)
    }

    override fun trySetText(text: String): Boolean {
        return node.trySetText(text)
    }

    override var text: String?
        get() = node.text
        set(v) {
            node.text = v
        }


    override fun focus(): Boolean {
        return node.focus()
    }

    override fun scrollForward(): Boolean {
        return node.scrollForward()
    }

    override fun scrollBackward(): Boolean {
        return node.scrollBackward()
    }

    override fun scrollLeft(): Boolean {
        return node.scrollLeft()
    }

    override fun swipe(dx: Int, dy: Int, delay: Int): Boolean {
        return node.swipe(dx, dy, delay)
    }

    override fun scrollRight(): Boolean {
        return node.scrollRight()
    }

    override val childs: Array<ViewNode>
        get() = node.childs

    override fun childAt(i: Int): ViewNode? {
        return node.childAt(i)
    }

    override val bounds: Rect?
        get() = node.bounds

    override val boundsInParent: Rect?
        get() = node.boundsInParent

    override val parent: ViewNode?
        get() = node.parent


    override fun getChildCount(): Int? {
        return node.getChildCount()
    }

    override fun isClickable(): Boolean {
        return node.isClickable()
    }

    override fun finder(): ViewFindBuilder {
        return node.finder()
    }

    companion object {
        val WAIT_MILLIS = 2000L
    }
}
