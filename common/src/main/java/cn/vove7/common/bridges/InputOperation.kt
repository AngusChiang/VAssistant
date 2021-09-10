package cn.vove7.common.bridges

/**
 * # InputOperation
 *
 * @author Vove
 * 2019/7/31
 */
interface InputOperation {

    fun sendKey(keyCode: Int)
    fun sendKeys(vararg keys: Int)

    val selectedText: String?

    fun inputText(text: CharSequence?): Boolean

    fun sendEnter()
    fun delete()
    fun deleteForward()
    fun select(start: Int, end: Int)
    fun selectAll()

}