package cn.vove7.common.view.finder

import android.graphics.Point
import android.graphics.Rect
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.accessibility.viewnode.ViewOperation
import cn.vove7.vtp.log.Vog
import java.lang.Thread.sleep

/**
 * # FindBuilder
 *
 * @author 17719
 * 2018/8/10
 */
open class FindBuilder : ViewOperation {
    val accessibilityService: AccessibilityApi? = AccessibilityApi.accessibilityService
    var finder: ViewFinder? = null

    constructor(finder: ViewFinder?) {
        this.finder = finder
    }

    constructor()

    /**
     * 找到第一个
     * @return ViewNode
     */
    fun findFirst(): ViewNode? {
        return finder?.findFirst()
    }

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
        val enterTime = System.currentTimeMillis()
        var now: Long
        while (findFirst() != null) {
            now = System.currentTimeMillis()
            if (now < enterTime + waitMs)
                sleep(100)
            else {
                Vog.d(this, "waitHide ---> 等待超时")
                return false
            }
        }
        return true
    }

    override fun tryClick(): Boolean {
        return findFirst()?.tryClick() == true
    }

    override fun click(): Boolean {
        return findFirst()?.tryClick() == true
    }

    override fun globalClick(): Boolean {
        return findFirst()?.globalClick() == true
    }

    override fun longClick(): Boolean {
        return findFirst()?.longClick() == true
    }

    override fun doubleClick(): Boolean {
        return findFirst()?.doubleClick() == true
    }

    override fun tryLongClick(): Boolean {
        return findFirst()?.tryLongClick() == true
    }

    override fun getCenterPoint(): Point? {
        return findFirst()?.getCenterPoint()
    }

    override fun select(): Boolean {
        return findFirst()?.select() == true
    }

    override fun trySelect(): Boolean {
        return findFirst()?.trySelect() == true
    }

    override fun scrollUp(): Boolean {
        return try {
            findFirst()?.scrollUp() == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }

    override fun scrollDown(): Boolean {
        return try {
            findFirst()?.scrollDown() == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun setText(text: String, ep: String?): Boolean {
        return findFirst()?.setText(text, ep) == true
    }

    override fun setText(text: String): Boolean {
        return findFirst()?.setText(text) == true
    }

    override fun appendText(s: String): Boolean {
        return findFirst()?.appendText(s) == true
    }

    override fun desc(): String? {
        return findFirst()?.desc()
    }

    override fun setTextWithInitial(text: String): Boolean {
        return findFirst()?.setTextWithInitial(text) == true
    }

    override fun trySetText(text: String): Boolean {
        return findFirst()?.trySetText(text) == true
    }

    override fun getText(): String? {
        return findFirst()?.getText()
    }

    override fun focus(): Boolean {
        return findFirst()?.focus() == true
    }

    override fun scrollForward(): Boolean {
        return findFirst()?.scrollForward() == true
    }

    override fun scrollBackward(): Boolean {
        return findFirst()?.scrollBackward() == true
    }

    override fun scrollLeft(): Boolean {
        return findFirst()?.scrollLeft() == true
    }

    override fun swipe(dx: Int, dy: Int, delay: Int): Boolean {
        return findFirst()?.swipe(dx, dy, delay) == true
    }

    override fun scrollRight(): Boolean {
        return findFirst()?.scrollRight() == true
    }

    override fun getChilds(): Array<ViewNode>? {
        return findFirst()?.getChilds()
    }

    override fun getBounds(): Rect? {
        return findFirst()?.getBounds()
    }

    override fun getBoundsInParent(): Rect? {
        return findFirst()?.getBoundsInParent()
    }

    override fun getParent(): ViewNode? {
        return findFirst()?.getParent()
    }

    override fun getChildCount(): Int? {
        return findFirst()?.getChildCount()
    }

    override fun isClickable(): Boolean {
        return findFirst()?.isClickable() ?: false
    }
}