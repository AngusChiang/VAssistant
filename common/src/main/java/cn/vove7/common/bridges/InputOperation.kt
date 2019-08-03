package cn.vove7.common.bridges

/**
 * # InputOperation
 *
 * @author Vove
 * 2019/7/31
 */
interface InputOperation {

    fun init()
    fun restore()

    fun sendKey(keyCode: Int)
    fun sendKeys(vararg keys: Int)

    fun sendDefaultEditorAction(): Boolean

    val selectedText: String?

    fun input(text: CharSequence?): Boolean
    fun actionSearch()


    fun actionGo()
    fun actionSend()

    fun actionDone()

    fun sendEnter()
    fun delete()
    fun deleteForward()
    fun select(start: Int, end: Int)
    fun selectAll()

    fun close()

}