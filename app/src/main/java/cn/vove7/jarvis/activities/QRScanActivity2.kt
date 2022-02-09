package cn.vove7.jarvis.activities

import android.content.Intent
import android.net.Uri
import cn.vove7.android.common.ext.invisible
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.TextHelper
import cn.vove7.jarvis.view.dialog.TextOperationDialog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.google.zxing.Result
import java.util.*

/**
 * # QrScanActivity2
 *
 * @author Vove
 * @date 2021/7/27
 */
class QRScanActivity2 : QRScanActivity() {

    override fun onScanResultCallback(result: Result?): Boolean {
        super.onScanResultCallback(result)
        result ?: return true
        cameraScan?.setAnalyzeImage(false)
        showResult(result.text)
        return true
    }

    private fun showResult(text: String) {
        MaterialDialog(this).show {
            title(text = "识别结果")
            message(text = text)
            noAutoDismiss()
            negativeButton(text = "复制") {
                SystemBridge.setClipText(text)
                GlobalApp.toastSuccess("已复制")
            }
            positiveButton(text = "退出") {
                it.dismiss()
                finishAndRemoveTask()
            }
            checkText(text)
            onDismiss {
                if (!isFinishing) {
                    pointView.invisible()
                    cameraScan.setAnalyzeImage(true)
                    cameraScan.startCamera()
                }
            }
        }
    }

    fun MaterialDialog.checkText(result: String) {
        when {
            result.startsWith("http", ignoreCase = true)
                    || result.matches(".*?://.*".toRegex()) -> {
                neutralButton(text = "访问") {
                    dismiss()
                    finish()
                    SystemBridge.openUrl(result.substring(0, 5).toLowerCase(Locale.ROOT) // 某些HTTP://
                            + result.substring(5))
                }
            }
            TextHelper.isEmail(result) -> {
                neutralButton(text = "发邮件") {
                    dismiss()
                    finish()
                    SystemBridge.sendEmail(result)
                }
            }
            result.startsWith("market:", ignoreCase = true) -> {
                neutralButton(text = "打开应用市场") {
                    dismiss()
                    finish()
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(result)
                    //跳转酷市场
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
            result.startsWith("smsto:", ignoreCase = true) -> {
                neutralButton(text = "发送短信") {
                    val ss = result.split(':')
                    val p = try {
                        ss[1]
                    } catch (e: Exception) {
                        GlobalApp.toastError("未发现手机号")
                        return@neutralButton
                    }
                    dismiss()
                    finish()
                    val content = try {
                        ss[2]
                    } catch (e: Exception) {
                        ""
                    }
                    SystemBridge.sendSMS(p, content)
                }
            }
            result.startsWith("tel:", ignoreCase = true) -> {
                neutralButton(text = "拨号") {
                    dismiss()
                    finish()
                    SystemBridge.call(result.substring(4))
                }
            }
            else -> {
                neutralButton(text = "编辑") {
                    TextOperationDialog(this@QRScanActivity2, TextOperationDialog.TextModel(result))
                }
            }
        }
    }

}