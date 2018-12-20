package cn.vove7.common.view.finder

import android.graphics.Point
import android.graphics.Rect
import cn.vove7.common.ViewNodeNotFoundException
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.accessibility.viewnode.ViewOperation
import cn.vove7.common.utils.whileWaitTime

/**
 * # FindBuilderWithOperation
 *
 * @author 17719
 * 2018/8/10
 */
abstract class FindBuilderWithOperation : ViewOperation {
    val accessibilityService: AccessibilityApi? = AccessibilityApi.accessibilityService
    var finder: ViewFinder? = null

    /**
     * 找到第一个
     * @return ViewNode
     */
    fun findFirst(): ViewNode? {
        return finder?.findFirst()
    }

    abstract fun waitFor(): ViewNode?

    abstract fun waitFor(m: Long): ViewNode?

    /**
     *
     * @return list
     */
    fun find(): Array<ViewNode> {
        return finder?.findAll() ?: emptyArray()
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
//        val enterTime = System.currentTimeMillis()
//        var now: Long
        return whileWaitTime(waitMs.toLong()) {
            if (findFirst() != null) true
            else null
        } ?: false
//        while (findFirst() != null) {
//            now = System.currentTimeMillis()
//            if (now < enterTime + waitMs)
//                sleep(100)
//            else {
//                Vog.d(this, "waitHide ---> 等待超时")
//                return false
//            }
//        }
//        return true
    }

    override fun tryClick(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).tryClick()
    }

    override fun click(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).tryClick()
    }

    override fun globalClick(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).globalClick()
    }

    override fun longClick(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).longClick()
    }

    override fun doubleClick(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).doubleClick()
    }

    override fun tryLongClick(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).tryLongClick()
    }

    override fun getCenterPoint(): Point? {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).getCenterPoint()
    }

    override fun select(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).select()
    }

    override fun trySelect(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).trySelect()
    }

    override fun scrollUp(): Boolean {
        return try {
            (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).scrollUp()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }

    override fun scrollDown(): Boolean {
        return try {
            (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).scrollDown()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun setText(text: String, ep: String?): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).setText(text, ep)
    }

    override fun setText(text: String): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).setText(text)
    }

    override fun appendText(s: String): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).appendText(s)
    }

    override fun desc(): String? {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).desc()
    }

    override fun setTextWithInitial(text: String): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).setTextWithInitial(text)
    }

    override fun trySetText(text: String): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).trySetText(text)
    }

    override fun getText(): String? {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).getText()
    }

    override fun focus(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).focus()
    }

    override fun scrollForward(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).scrollForward()
    }

    override fun scrollBackward(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).scrollBackward()
    }

    override fun scrollLeft(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).scrollLeft()
    }

    override fun swipe(dx: Int, dy: Int, delay: Int): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).swipe(dx, dy, delay)
    }

    override fun scrollRight(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).scrollRight()
    }

    override fun getChilds(): Array<ViewNode>? {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).getChilds()
    }

    override fun getBounds(): Rect? {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).getBounds()
    }

    override fun getBoundsInParent(): Rect? {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).getBoundsInParent()
    }

    override fun getParent(): ViewNode? {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).getParent()
    }

    override fun getChildCount(): Int? {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).getChildCount()
    }

    override fun isClickable(): Boolean {
        return (waitFor(2000) ?: throw ViewNodeNotFoundException(finder)).isClickable()
    }
}