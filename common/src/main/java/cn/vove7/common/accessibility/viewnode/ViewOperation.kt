package cn.vove7.common.accessibility.viewnode

import android.graphics.Point
import android.graphics.Rect
import cn.vove7.common.annotation.ScriptApiClass
import cn.vove7.common.view.finder.ViewFindBuilder

/**
 *
 */
@ScriptApiClass("ViewNode")
interface ViewOperation {
    /**
     * 尝试点击
     * 当此节点点击失败，会尝试向上级容器尝试
     * @return Boolean 是否成功
     */
    fun tryClick(): Boolean

    /**
     * 获取中心点坐标(绝对)
     * @return Point?
     */
    fun getCenterPoint(): Point?

    /**
     * 获取下级所有Node
     * @return Array<ViewNode>
     */
    val childs: Array<ViewNode>

    /**
     * 获取边界范围
     * @return Rect
     */
    val bounds: Rect?

    /**
     * 获取基于父级容器边界范围
     * @return Rect
     */
    val boundsInParent: Rect?

    /**
     * 获取父级Node
     * @return ViewNode?
     */
    val parent: ViewNode?

    /**
     * 点击此Node
     * 失败率较高，使用起来不方便
     * @return Boolean
     */
    fun click(): Boolean

    /**
     * 使用全局函数click进行点击操作，如点击网页控件
     * 需要7.0+
     * @return Boolean
     */
    fun globalClick(): Boolean

    /**
     * 以此Node中心滑动到dx,dy的地方
     * setScreenSize() 对此有效
     * @param dx Int x方向 移动距离 ±
     * @param dy Int y ±
     * @param delay Int 用时
     * @return Boolean
     */
    fun swipe(dx: Int, dy: Int, delay: Int): Boolean

    /**
     * 尝试长按，机制类似tryClick
     * @return Boolean
     */
    fun tryLongClick(): Boolean

    /**
     * 获取下级Node数量
     * @return Int?
     */
    fun getChildCount(): Int?

    fun childAt(i: Int): ViewNode?
    /**
     * 长按操作
     * @return Boolean
     */
    fun longClick(): Boolean

    /**
     * 双击操作
     * 默认使用tryClick
     * @return Boolean
     */
    fun doubleClick(): Boolean

    /**
     * 尝试设置文本内容，机制同tryClick
     * @param text String
     * @return Boolean 是否成功
     */
    fun trySetText(text: CharSequence): Boolean

    /**
     * 获取Node包含文本
     * @return String?
     */
    var text: CharSequence?

    /**
     * 追加文本
     * @param s String
     * @return Boolean
     */
    fun appendText(s: CharSequence)

    fun desc(): CharSequence?
    //选择
    fun select(): Boolean

    fun trySelect(): Boolean

    //获得焦点
    fun focus(): Boolean

    /***以下不常用***/

    //一般
    fun scrollUp(): Boolean

    fun scrollDown(): Boolean

    fun setText(text: CharSequence, ep: String?): Boolean
    /**
     * 拼音首字母
     */
    fun setTextWithInitial(text: CharSequence): Boolean

    fun scrollForward(): Boolean
    fun scrollBackward(): Boolean
    fun scrollLeft(): Boolean
    fun scrollRight(): Boolean

    fun isClickable(): Boolean

    fun finder(): ViewFindBuilder
}