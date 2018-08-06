package cn.vove7.common.viewnode

/**
 *
 */
interface ViewOperation {
    fun tryClick(): Boolean
    fun click(): Boolean
    fun longClick(): Boolean
    fun doubleClick(): Boolean
    fun tryLongClick(): Boolean
    fun select(): Boolean
    fun trySelect(): Boolean
    fun scrollUp(): Boolean
    fun scrollDown(): Boolean
    fun setText(text: String, ep: String?): Boolean
    fun setText(text: String): Boolean
    fun trySetText(text: String): Boolean
    fun getText(): String?
    fun focus(): Boolean
    fun scrollForward(): Boolean
    fun scrollBackward(): Boolean
    fun scrollLeft(): Boolean
    fun scrollRight(): Boolean

    fun await()
}