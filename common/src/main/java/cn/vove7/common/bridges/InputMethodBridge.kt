@file:Suppress("MemberVisibilityCanBePrivate")

package cn.vove7.common.bridges

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.inputmethod.*
import cn.vove7.common.NeedAccessibilityException
import cn.vove7.common.R
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.activities.RunnableActivity.Companion.runInShellActivity
import cn.vove7.common.app.AppPermission
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.service.AssistInputService
import cn.vove7.common.utils.newTask
import cn.vove7.common.utils.whileWaitTime
import cn.vove7.common.view.finder.ViewFindBuilder.Companion.text
import cn.vove7.vtp.log.Vog
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * # InputMethodBridge
 *
 * @author Vove
 * 2019/7/30
 */
@SuppressLint("StaticFieldLeak")
object InputMethodBridge : InputOperation {

    ////////////TODO  fix getext select
    //test all api

    private val app: Context get() = GlobalApp.APP

    private var inputService: InputMethodService? = null

    private val editorInfo: EditorInfo? get() = inputService?.currentInputEditorInfo

    val inputConnection: InputConnection
        get() = inputService?.currentInputConnection ?: UselessInputConnection()


    private val imm get() = (GlobalApp.APP.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)

    private fun showInputMethodPicker() {
        imm.showInputMethodPicker()
    }

    val currentIM: String
        get() = Settings.Secure.getString(app.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD) ?: ""


    /**
     * 启用的输入法
     */
    val enabledInputMethodList: Array<InputMethodInfo> get() = imm.enabledInputMethodList.toTypedArray()


    override fun sendKey(keyCode: Int) {
        init()
        val eventTime = SystemClock.uptimeMillis()
        inputConnection.sendKeyEvent(KeyEvent(eventTime, eventTime,
                KeyEvent.ACTION_DOWN, keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD or KeyEvent.FLAG_KEEP_TOUCH_MODE))

        inputConnection.sendKeyEvent(KeyEvent(eventTime, SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP, keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD or KeyEvent.FLAG_KEEP_TOUCH_MODE))

    }

    override fun sendKeys(vararg keys: Int) {
        init()
        val eventTime = SystemClock.uptimeMillis()
        keys.forEach {
            inputConnection.sendKeyEvent(KeyEvent(eventTime, eventTime,
                    KeyEvent.ACTION_DOWN, it, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD or KeyEvent.FLAG_KEEP_TOUCH_MODE))
        }
        keys.forEach {
            inputConnection.sendKeyEvent(KeyEvent(eventTime, SystemClock.uptimeMillis(),
                    KeyEvent.ACTION_UP, it, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD or KeyEvent.FLAG_KEEP_TOUCH_MODE))

        }
    }

    override fun sendDefaultEditorAction(): Boolean {
        init()
        return inputService?.sendDefaultEditorAction(false) ?: throw Exception("VAssist 输入法绑定编辑框失败")
    }


    override fun input(text: CharSequence?): Boolean {
        init()
        return inputConnection.commitText(text, -1)
    }

    override val selectedText: String?
        get() {
            init()
            return inputConnection.getSelectedText(0)?.toString()
        }

    override fun actionSearch() {
        sendActon(EditorInfo.IME_ACTION_SEARCH)
    }

    override fun actionGo() {
        sendActon(EditorInfo.IME_ACTION_GO)
    }

    override fun actionSend() {
        sendActon(EditorInfo.IME_ACTION_SEND)
    }

    fun sendActon(action: Int) {
        init()
        inputConnection.performEditorAction(action)
    }

    override fun actionDone() {
        sendActon(EditorInfo.IME_ACTION_DONE)
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

    override fun close() {
        sendKey(KeyEvent.KEYCODE_BACK)
    }

    override fun select(start: Int, end: Int) {
        init()
        inputConnection.setSelection(start, end)
    }

    override fun selectAll() {
        select(0, Int.MAX_VALUE)
    }

    fun bind(service: InputMethodService) {
        inputService = service
        Vog.d("active --> $inputService")
    }

    fun unbind() {
        inputService = null
        Vog.d("unbind")
    }

    var storeIm: String? = null

    val isEnable: Boolean
        get() = {
            enabledInputMethodList.find { it.id.endsWith(".AssistInputService") } != null
        }()

    /**
     * 初始化，切换软键盘为VAssist
     */
    override fun init() {
        if (storeIm != null) return
        if (!isEnable) {
            GlobalApp.toastInfo("当前未开启输入法，请开启VAssist输入法")
            app.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).newTask())
            return
        }
        val cim = currentIM
        storeIm = cim
        Vog.d("当前输入法： $cim")
        if (cim.endsWith(".AssistInputService")) {
            return
        }

        when {
            AppPermission.canWriteSecureSettings -> {
                setByWriteSecureSettings()
            }
            RootHelper.hasRoot(500) -> {
                Vog.d("使用Root权限 切换输入法")
                RootHelper.execWithSu("settings put secure default_input_method ${app.packageName}/${AssistInputService::class.java.name}")
            }
            else -> setByAccessibility()
        }
        waitServiceOnline()
    }

    /**
     * 等待服务上线
     */
    private fun waitServiceOnline() {
        val waitResult = whileWaitTime(5000) {
            sleep(100)
            inputService
        }

        Vog.d("waitServiceOnline : $waitResult $inputService")
        if (inputService == null) {
            throw Exception("输入法切换失败")
        }
    }

    private fun setByAccessibility() {
        Vog.d("使用无障碍服务 切换输入法")
        if (!AccessibilityApi.isBaseServiceOn) {
            AppBus.post(RequestPermission("无障碍服务"))
            throw NeedAccessibilityException()
        }
        val countDownLatch = CountDownLatch(1)
        //in shellActivity
        runInShellActivity {
            try {
                sleep(500)
                showInputMethodPicker()
                sleep(500)//等待显示

                text(app.getString(R.string.app_name)).waitFor(2000)!!.tryClick()
                sleep(500)
            } finally {
                it.finishAndRemoveTask()
                //等待消失
                sleep(500)
                countDownLatch.countDown()
            }

        }
        if (!countDownLatch.await(5, TimeUnit.SECONDS)) {
            GlobalApp.toastError("输入法切换失败")
            throw Exception("输入法切换失败")
        }
    }

    private fun setByWriteSecureSettings() {
        Vog.d("使用WriteSecureSettings权限 切换输入法")
        Settings.Secure.putString(app.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD, "${app.packageName}/${AssistInputService::class.java.name}")
    }

    /**
     * 恢复原键盘
     */
    @Synchronized
    override fun restore() {
        val sim = storeIm
        Vog.d("恢复输入法： $sim")
        if (sim == null || currentIM == sim) {
            storeIm = null
            return
        }
        when {
            AppPermission.canWriteSecureSettings -> restoreByWriteSecureSettings(sim)
            RootHelper.hasRoot(500) -> RootHelper.execWithSu("settings put secure default_input_method $sim")
            else -> restoreByAccessibility(sim)
        }
        storeIm = null
    }

    private fun restoreByWriteSecureSettings(id: String) {
        Settings.Secure.putString(app.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD, id)
    }

    private fun restoreByAccessibility(id: String) {

        runInShellActivity {
            try {
                sleep(300)
                showInputMethodPicker()
                sleep(300)//等待显示

                getStoreImLabel(id)?.also { name ->
                    text(name).waitFor(1000)?.tryClick()
                }

                Vog.d("输入法恢复成功")
            } finally {
                it.finishAndRemoveTask()
                //等待消失
                sleep(300)
            }
        }
    }

    private fun getStoreImLabel(id: String): String? = {
        enabledInputMethodList.find { it.id == id }
                ?.loadLabel(app.packageManager)?.toString()
    }()

}

class UselessInputConnection : InputConnection {

    private val err = "VAssist 输入法绑定编辑框失败"

    override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean = throw Exception(err)


    override fun closeConnection() = throw Exception(err)


    override fun commitCompletion(text: CompletionInfo?): Boolean = throw Exception(err)


    override fun setComposingRegion(start: Int, end: Int): Boolean = throw Exception(err)


    override fun performContextMenuAction(id: Int): Boolean = throw Exception(err)


    override fun setSelection(start: Int, end: Int): Boolean = throw Exception(err)


    override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean = throw Exception(err)


    override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence? = throw Exception(err)
    override fun getHandler(): Handler? = throw Exception(err)

    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean = throw Exception(err)

    override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText? = throw Exception(err)

    override fun beginBatchEdit(): Boolean = throw Exception(err)


    override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean = throw Exception(err)


    override fun clearMetaKeyStates(states: Int): Boolean = throw Exception(err)


    override fun endBatchEdit(): Boolean = throw Exception(err)


    override fun getSelectedText(flags: Int): CharSequence? = throw Exception(err)

    override fun reportFullscreenMode(enabled: Boolean): Boolean = throw Exception(err)


    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean = throw Exception(err)


    override fun getCursorCapsMode(reqModes: Int): Int = throw Exception(err)

    override fun getTextAfterCursor(n: Int, flags: Int): CharSequence? = throw Exception(err)

    override fun performPrivateCommand(action: String?, data: Bundle?): Boolean = throw Exception(err)


    override fun sendKeyEvent(event: KeyEvent?): Boolean = throw Exception(err)


    override fun finishComposingText(): Boolean = throw Exception(err)


    override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean = throw Exception(err)


    override fun commitContent(inputContentInfo: InputContentInfo, flags: Int, opts: Bundle?): Boolean = throw Exception(err)


    override fun performEditorAction(editorAction: Int): Boolean = throw Exception(err)

}