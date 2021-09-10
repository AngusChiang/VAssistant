@file:Suppress("MemberVisibilityCanBePrivate")

package cn.vove7.common.bridges

import android.annotation.SuppressLint
import android.view.KeyEvent
import cn.vove7.common.MessageException


/**
 * # InputMethodBridge
 *
 * @author Vove
 * 2019/7/30
 */
@SuppressLint("StaticFieldLeak")
object InputMethodBridge : InputOperation {

    override fun sendKey(keyCode: Int) {
        SystemBridge.sendKey(keyCode)
    }

    override fun sendKeys(vararg keys: Int) {
        throw MessageException("暂不支持此操作")
    }

    override fun inputText(text: CharSequence?): Boolean {
        ShellHelper.execAuto("input text $text")
        return true
    }

    override val selectedText: String?
        get() {
            throw MessageException("暂不支持此操作")
        }

    override fun sendEnter() {
        sendKey(KeyEvent.KEYCODE_ENTER)
    }

    override fun delete() {
        sendKey(KeyEvent.KEYCODE_DEL)
    }

    override fun deleteForward() {
        sendKey(KeyEvent.KEYCODE_FORWARD_DEL)
    }

    override fun select(start: Int, end: Int) {
        throw MessageException("暂不支持此操作")
    }

    override fun selectAll() {
        select(0, Int.MAX_VALUE)
    }

}
