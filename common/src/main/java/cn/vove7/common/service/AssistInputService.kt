package cn.vove7.common.service

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import cn.vove7.common.R
import cn.vove7.common.bridges.InputMethodBridge
import cn.vove7.vtp.log.Vog

/**
 * # AssistInputService
 *
 * @author Vove
 * 2019/7/30
 */
class AssistInputService : InputMethodService() {

    /**
     * 在弹出时 触发
     */
    override fun onCreate() {
        super.onCreate()
        Vog.d("onCreate")
    }

    lateinit var keyboardView: KeyboardView

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.input_method_pannel, null) as KeyboardView
        keyboardView.keyboard = Keyboard(this, R.xml.keyboard)
        return keyboardView
    }

    override fun onBindInput() {
        super.onBindInput()
        InputMethodBridge.bind(this)
        Vog.d("onBind")
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        attribute?.dump({ Vog.d(it) }, "EditorInfo -----> ")
        super.onStartInput(attribute, restarting)
    }

    override fun onUnbindInput() {
        super.onUnbindInput()
        InputMethodBridge.unbind()
        Vog.d("onUnbindInput")
    }
}