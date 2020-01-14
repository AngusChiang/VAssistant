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
import cn.vove7.common.utils.CoroutineExt
import cn.vove7.common.utils.broadcastImageFile
import cn.vove7.common.utils.clearTask
import cn.vove7.common.utils.newTask
import cn.vove7.common.view.finder.ViewFindBuilder.Companion.text
import cn.vove7.common.view.finder.ViewFindBuilder.Companion.type
import kotlinx.coroutines.delay
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
    fun qrWithWechat(path: String) = CoroutineExt.launch {
        var tmpFile: File? = null
        try {
            tmpFile = createTmpFile
            File(path).copyTo(tmpFile, true)//复制文件
            tmpFile.broadcastImageFile()

            doQrWithWechat()
        } catch (e: Throwable) {
            tmpFile?.delete()
            tmpFile = null
            GlobalApp.toastError("执行失败: " + e.message)
        } finally {
            //立即删除会导致无法识别
            delay(2000)
            tmpFile?.delete()
        }
    }

    val app get() = GlobalApp.APP

    private suspend fun doQrWithWechat() {
        val intent = app.packageManager.getLaunchIntentForPackage("com.tencent.mm")
        if (intent == null) {
            GlobalApp.toastError("未安装微信")
            return
        }
        AccessibilityApi.requireAccessibility()
        intent.putExtra("LauncherUI.From.Scaner.Shortcut", true)
        app.startActivity(intent.clearTask())

        text("扫二维码 / 条码 / 小程序码").parent!!.children[2].click()
        delay(800)
        type("RecyclerView").children[1].click()
    }

    /**
     * 调用支付宝扫一扫
     * @param path String
     */
    fun qrWithAlipay(path: String) = CoroutineExt.launch {
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

    fun spotWithTaobao(path: String) {
        //ResolveInfo{bf02772 com.taobao.taobao/com.etao.feimagesearch.IrpActivity m=0x608000}
        spotWith(path, ComponentName("com.taobao.taobao", "com.etao.feimagesearch.IrpActivity"), "手机淘宝")
    }

    fun spotWithJD(path: String) {
        //ResolveInfo{cbd9e43 com.jingdong.app.mall/.open.InterfaceActivity m=0x608000}
        spotWith(path, ComponentName("com.jingdong.app.mall", "com.jingdong.app.mall.open.InterfaceActivity"), "京东")
    }

    private fun spotWith(path: String, cn: ComponentName, appName: String) {
        if (!SystemBridge.hasInstall(cn.packageName)) {
            GlobalApp.toastError("未安装${appName}")
            return
        }
        val tmpFile = File(path)
        val imgUri = if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", tmpFile)
        } else Uri.fromFile(tmpFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpg"
            putExtra(Intent.EXTRA_STREAM, imgUri)
            component = cn
        }
        app.startActivity(intent.newTask())
    }
}