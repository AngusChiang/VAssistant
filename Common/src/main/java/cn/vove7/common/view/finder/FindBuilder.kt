package cn.vove7.common.view.finder

import android.graphics.Point
import android.graphics.Rect
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.accessibility.viewnode.ViewNode
import cn.vove7.common.accessibility.viewnode.ViewOperation

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


    override fun tryClick(): Boolean {
        val node = findFirst()
        return node?.tryClick() == true
    }

    override fun click(): Boolean {
        val node = findFirst()
        return node?.tryClick() == true
    }

    override fun longClick(): Boolean {
        val node = findFirst()
        return node?.longClick() == true
    }

    override fun doubleClick(): Boolean {
        val node = findFirst()
        return node?.doubleClick() == true
    }

    override fun tryLongClick(): Boolean {
        val node = findFirst()
        return node?.tryLongClick() == true
    }

    override fun getCenterPoint(): Point? {
        val node = findFirst()
        return node?.getCenterPoint()
    }

    override fun select(): Boolean {
        val node = findFirst()
        return node?.select() == true
    }

    override fun trySelect(): Boolean {
        val node = findFirst()
        return node?.trySelect() == true
    }

    override fun scrollUp(): Boolean {
        val node = findFirst()
        try {
            return node?.scrollUp() == true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    override fun scrollDown(): Boolean {
        val node = findFirst()
        return try {
            node?.scrollDown() == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }

    override fun setText(text: String, ep: String?): Boolean {
        val node = findFirst()
        return node?.setText(text, ep) == true
    }

    override fun setText(text: String): Boolean {
        val node = findFirst()
        return node?.setText(text) == true
    }

    override fun setTextWithInitial(text: String): Boolean {
        val node = findFirst()
        return node?.setTextWithInitial(text) == true

    }

    override fun trySetText(text: String): Boolean {
        val node = findFirst()
        return node?.trySetText(text) == true
    }

    override fun getText(): String? {
        val node = findFirst()
        return node?.getText()
    }

    override fun focus(): Boolean {
        val node = findFirst()
        return node?.focus() == true
    }

    override fun scrollForward(): Boolean {
        val node = findFirst()
        return node?.scrollForward() == true
    }

    override fun scrollBackward(): Boolean {
        val node = findFirst()
        return node?.scrollBackward() == true
    }

    override fun scrollLeft(): Boolean {
        val node = findFirst()
        return node?.scrollLeft() == true
    }

    override fun swipe(dx: Int, dy: Int, delay: Int): Boolean {
        val node = findFirst()
        return node?.swipe(dx, dy, delay) == true
    }

    override fun scrollRight(): Boolean {
        val node = findFirst()
        return node?.scrollRight() == true
    }

    override fun getChilds(): Array<ViewNode>? {
        val node = findFirst()
        return node?.getChilds()
    }

    override fun getBounds(): Rect? {
        val node = findFirst()
        return node?.getBounds()
    }

    override fun getBoundsInParent(): Rect? {
        val node = findFirst()
        return node?.getBoundsInParent()
    }

    override fun getParent(): ViewNode? {
        val node = findFirst()
        return node?.getParent()
    }

    override fun getChildCount(): Int? {
        val node = findFirst()
        return node?.getChildCount()
    }

    override fun isClickable(): Boolean {
        val node = findFirst()
        return node?.isClickable() ?: false
    }
}