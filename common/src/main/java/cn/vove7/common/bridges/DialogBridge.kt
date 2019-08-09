package cn.vove7.common.bridges

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.view.View
import android.widget.EditText
import cn.vove7.common.MessageException
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.model.ResultBox
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.view.dialog.BaseDialog

/**
 * # DialogBridge
 *
 * @author Vove
 * 2019/8/8
 */
object DialogBridge {

    fun alert(title: String?, message: String?): Boolean {
        val resultBox = ResultBox<Boolean>()
        runOnUi {
            val context = GlobalApp.APP
            val alertDialog = AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("继续") { _, _ ->
                        resultBox.setAndNotify(true)
                    }.setNegativeButton("取消") { _, _ ->
                        resultBox.setAndNotify(false)
                    }.create()

            alertDialog?.setFloat()
            alertDialog?.show()
            //TODO 语音控制
//                if (AppConfig.voiceControlDialog) {
//                    voiceMode = MODE_ALERT
//                    startRecog()
//                }

        }
        return resultBox.blockedGet(false)!!
    }

    fun input(title: String?, hint: String?): String? {
        return input(title, hint, null)
    }

    fun input(title: String?, hint: String?, preText: String?): String? {
        val resultBox = ResultBox<String?>()
        runOnUi {
            val app = GlobalApp.APP
            BaseDialog(app).apply {
                val editText = EditText(app)
                editText.hint = hint
                editText.setText(preText)
                setContentView(editText)
                setTitle(title ?: "")
                setCancelable(false)
                setButton(DialogInterface.BUTTON_NEGATIVE, "取消", View.OnClickListener {
                    resultBox.setAndNotify(null)
                })
                setButton(DialogInterface.BUTTON_POSITIVE, "确定", View.OnClickListener {
                    resultBox.setAndNotify(editText.text.toString())
                })
                setOnDismissListener {
                    resultBox.setAndNotify(null)
                }
                setFloat()
                show()
            }
        }
        return resultBox.blockedGet(false)
    }

}

internal fun Dialog.setFloat() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        if (!cn.vove7.vtp.runtimepermission.PermissionUtils.canDrawOverlays(context)) {
            AppBus.post(RequestPermission("悬浮窗权限"))
            throw MessageException("无悬浮窗权限")
        }
    }
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        window?.setType(android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
    } else {
        window?.setType(android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
    }
}