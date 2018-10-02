package cn.vove7.common.accessibility.viewnode

import android.graphics.Point
import android.graphics.Rect

/**
 *
 */
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
    fun getChilds(): Array<ViewNode>?

    /**
     * 获取边界范围
     * @return Rect
     */
    fun getBounds(): Rect?

    /**
     * 获取基于父级容器边界范围
     * @return Rect
     */
    fun getBoundsInParent(): Rect?

    /**
     * 获取父级Node
     * @return ViewNode?
     */
    fun getParent(): ViewNode?

    /**
     * 点击此Node
     * 失败率较高，使用起来不方便
     * @return Boolean
     */
    fun click(): Boolean

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
     * 设置文本,一般只能用于可编辑控件
     * @param text String
     * @return Boolean
     */
    fun setText(text: String): Boolean

    /**
     * 尝试设置文本内容，机制同tryClick
     * @param text String
     * @return Boolean 是否成功
     */
    fun trySetText(text: String): Boolean

    /**
     * 获取Node包含文本
     * @return String?
     */
    fun getText(): String?

    //选择
    fun select(): Boolean

    fun trySelect(): Boolean

    //获得焦点
    fun focus(): Boolean

    /***以下不常用***/

    //一般
    fun scrollUp(): Boolean

    fun scrollDown(): Boolean

    fun setText(text: String, ep: String?): Boolean
    /**
     * 拼音首字母
     */
    fun setTextWithInitial(text: String): Boolean

    fun scrollForward(): Boolean
    fun scrollBackward(): Boolean
    fun scrollLeft(): Boolean
    fun scrollRight(): Boolean

    fun isClickable(): Boolean
}