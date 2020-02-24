package cn.vove7.jarvis.tools

import android.content.ComponentName
import android.content.Intent
import android.os.Environment
import cn.vove7.common.MessageException
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.log
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.CoroutineExt
import cn.vove7.common.utils.broadcastImageFile
import cn.vove7.common.utils.clearTask
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
            e.log()
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
            throw MessageException("未安装微信")
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
    fun qrWithAlipay(path: String): Boolean = shareWith(path, ComponentName("com.eg.android.AlipayGphone",
            "com.alipay.mobile.quinox.splash.ShareScanQRDispenseActivity"), "支付宝")

    fun spotWithTaobao(path: String): Boolean {
        //ResolveInfo{bf02772 com.taobao.taobao/com.etao.feimagesearch.IrpActivity m=0x608000}
        return shareWith(path, ComponentName("com.taobao.taobao", "com.etao.feimagesearch.IrpActivity"), "手机淘宝")
    }

    fun spotWithJD(path: String): Boolean {
        //ResolveInfo{cbd9e43 com.jingdong.app.mall/.open.InterfaceActivity m=0x608000}
        return shareWith(path, ComponentName("com.jingdong.app.mall", "com.jingdong.app.mall.open.InterfaceActivity"), "京东")
    }

    private fun shareWith(path: String, cn: ComponentName, appName: String): Boolean {
        if (!SystemBridge.hasInstall(cn.packageName)) {
            GlobalApp.toastError("未安装${appName}")
            return false
        }
        val imgUri = File(path).toShareUri()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpg"
            putExtra(Intent.EXTRA_STREAM, imgUri)
            component = cn
        }
        //已经检查 component
        app.startActivity(intent)
        return true
    }

    fun ocrWithBaiMiao(screenPath: String): Boolean {
        return shareWith(screenPath, ComponentName("com.uzero.baimiao",
                "com.uzero.baimiao.ui.ImageCropperAndRecognizeActivity"), "白描")
    }
}