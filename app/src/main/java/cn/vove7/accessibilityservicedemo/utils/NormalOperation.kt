package cn.vove7.accessibilityservicedemo.utils

interface NormalOperation {
    fun click(): Boolean
    fun longClick(): Boolean
    fun select(): Boolean
    fun scrollUp(): Boolean
    fun scrollDown(): Boolean
    fun setText(text: String): Boolean
    fun getText(): String?
    fun focus(): Boolean
}
