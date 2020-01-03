package cn.vove7.jarvis.tools

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.*
import cn.vove7.common.view.finder.ViewFindBuilder.Companion.text
import cn.vove7.common.view.finder.ViewFindBuilder.Companion.types
import java.io.File
import java.util.*

/**
 * # ActionHelper
 *
 * @author 11324
 * 2019/4/20
 */
object ActionHelper {
    private val createTmpFile
        get() = File(Environment.getExternalStorageDirectory(),
                "tmp${Random().nextInt(100)}.png")

    /**
     * 使用微信扫描屏幕
     */
    fun qrWithWechat(path: String) {
        CoroutineExt.launch {
            var tmpFile: File? = null
            try {
                tmpFile = createTmpFile
                File(path).copyTo(tmpFile, true)//复制文件
                tmpFile.broadcastImageFile()

                doQrWithWechat()
            } catch (e: Throwable) {
                GlobalApp.toastError("执行失败: " + e.message)
            } finally {
                runOnNewHandlerThread(delay = 3000) {
                    tmpFile?.delete()
                }
            }
        }
    }

    val app get() = GlobalApp.APP

    private fun doQrWithWechat() {
        val intent = app.packageManager.getLaunchIntentForPackage("com.tencent.mm")
        if (intent == null) {
            GlobalApp.toastError("未安装微信")
            return
        }
        AccessibilityApi.waitAccessibility()
        intent.putExtra("LauncherUI.From.Scaner.Shortcut", true)
        app.startActivity(intent.clearTask())

        val p = text("二维码/条码").waitFor()?.parent?.parent?.parent!!

        p.finder().depths(arrayOf(1, 0, 0, 0)).tryClick()

        text("从相册选取二维码").waitFor(5000)?.tryClick()

        text("图片").waitFor(15000)//等待进入 选择图片

        types("GridView").childs[1].tryClick()

    }

    /**
     * 调用支付宝扫一扫
     * @param path String
     */
    fun qrWithAlipay(path: String) {
        CoroutineExt.launch {
            try {
                if (SystemBridge.getAppInfo("com.eg.android.AlipayGphone") == null) {
                    GlobalApp.toastError("未安装支付宝")
                    return@launch
                }
                val tmpFile = File(path)

                val imgUri = if (Build.VERSION.SDK_INT >= 24) {
                    FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", tmpFile)
                } else Uri.fromFile(tmpFile)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpg"
                    putExtra(Intent.EXTRA_STREAM, imgUri)
                    component = ComponentName("com.eg.android.AlipayGphone", "com.alipay.mobile.quinox.splash.ShareScanQRDispenseActivity")
                }
                app.startActivity(intent.newTask())

            } catch (e: Throwable) {
                GlobalApp.toastError("执行失败: " + e.message)
            }
        }
    }

    fun spotWithTaobao(path: String) {
        if (!SystemBridge.hasInstall("com.taobao.taobao")) {
            GlobalApp.toastError("未安装手机淘宝")
            return
        }
        val tmpFile = File(path)

        val imgUri = if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", tmpFile)
        } else Uri.fromFile(tmpFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpg"
            putExtra(Intent.EXTRA_STREAM, imgUri)
            //ResolveInfo{bf02772 com.taobao.taobao/com.etao.feimagesearch.IrpActivity m=0x608000}
            component = ComponentName("com.taobao.taobao", "com.etao.feimagesearch.IrpActivity")
        }
        app.startActivity(intent.newTask())
    }
}