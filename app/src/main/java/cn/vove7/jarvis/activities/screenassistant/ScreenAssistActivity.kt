package cn.vove7.jarvis.activities.screenassistant

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.BottomSheetBehavior
import android.view.View
import android.view.View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION
import android.view.animation.AlphaAnimation
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.bridges.UtilBridge
import cn.vove7.common.utils.*
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.TextOcrActivity
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.DataCollector
import cn.vove7.jarvis.tools.QRTools
import cn.vove7.jarvis.tools.Tutorials
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.view.bottomsheet.AssistSessionGridController
import cn.vove7.jarvis.view.dialog.ImageClassifyResultDialog
import cn.vove7.vtp.log.Vog
import kotlinx.android.synthetic.main.dialog_assist.*
import java.io.File
import java.util.*

/**
 * # ScreenAssistActivity
 * 屏幕助手Activity
 * @author 11324
 * 2019/3/21
 */
class ScreenAssistActivity : BaseActivity() {

    private lateinit var screenPath: String
    private lateinit var bottomController: AssistSessionGridController

    companion object {
        fun createIntent(path: String? = null): Intent {
            return Intent(GlobalApp.APP.packageName + ".SCREEN_ASSIST").apply {
                newTask()
                path?.also {
                    putExtra("path", it)
                }
            }
        }
    }

    private val isReady: Boolean?
        get() =
            if (showProgressBar) null else true

    private var showProgressBar: Boolean = false
        set(value) {
            field = value
            runOnUi {
                if (value) progress_bar.visibility = View.VISIBLE
                else progress_bar.visibility = View.INVISIBLE
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_assist)

        window.setWindowAnimations(R.style.ScreenAssist)
        showProgressBar = true
        bottomController = AssistSessionGridController(this, bottom_sheet, itemClick) {
            if (isReady == true) screenPath else null
        }
        bottomController.initView()
        bottomController.hideBottom()
        bottomController.bottomView.visibility = View.GONE

        bottomController.bottomView.post {
            bottomController.behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(p0: View, p1: Int) {
                    if (p1 == BottomSheetBehavior.STATE_HIDDEN) {
                        Vog.d("onStateChanged ---> 隐藏")
                        finish()
                    }
                }

                override fun onSlide(p0: View, p1: Float) {}
            })
        }

        voice_btn.setOnClickListener {
            MainService.switchRecog()
            bottomController.hideBottom()
        }
        handlerScreen()

        root.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun showView() {
        runOnUi {
            if (!bottomController.isBottomSheetShowing) {
                bottomController.bottomView.visibility = View.VISIBLE
                bottomController.showBottom()

                val animation = AlphaAnimation(0f, 1f)
                animation.duration = 300
                bottomController.bottomView.startAnimation(animation)
                bottomController.bottomView.post {
                    val list = arrayListOf<View>()
                    bottomController.bottomView.findViewsWithText(list, "二维码/条码识别", FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
                    Tutorials.showForView(this, Tutorials.screen_assistant_qrcode,
                            list[0], "长按查看更多功能", "使用微信扫一扫\n支付宝扫一扫")

                }
            }
        }
    }


    private val cachePath
        get() = cacheDir.absolutePath +
                "/screen-${Random().nextInt(999)}.png"

    private fun afterHandleScreen() {
        runOnUi {
            checkFuns()
        }
        //进入时清空缓存
        ThreadPool.runOnCachePool {
            cacheDir.listFiles()?.filter { it.isFile && it.absolutePath != screenPath }?.forEach {
                it.delete()
            }
        }
    }


    private fun handlerScreen() {
        intent?.apply {
            if (hasExtra("path")) {
                screenPath = getStringExtra("path")
                showProgressBar = false
                afterHandleScreen()
            } else {
                runOnNewHandlerThread {
                    val path = SystemBridge.screenShot()?.let {
                        runOnUi {
                            //截完图显示面板
                            showView()
                        }
                        SystemBridge.release()
                        UtilBridge.bitmap2File(it, cachePath)?.absolutePath
                    }

                    if (path == null) {
                        GlobalApp.toastError("截图失败")
                        finish()
                        return@runOnNewHandlerThread
                    } else screenPath = path
                    showProgressBar = false
                    afterHandleScreen()
                }
            }
        }
    }

    private fun checkFuns() {
        intent?.apply {
            if (hasExtra("fun_id")) {
                val i = getIntExtra("fun_id", -1)
                itemClick.invoke(i)
            } else {
                showView()
            }
        }
    }

    override fun onBackPressed() {
        if (bottomController.isBottomSheetShowing) {
            bottomController.hideBottom()
        } else {
            finish()
        }
    }

    private val funMap = mapOf(
            Pair(0, {
                DataCollector.buriedPoint("sa_1")
                imageClassify()
            }),
            Pair(1, {
                DataCollector.buriedPoint("sa_2")
                screenOcr()
            }),
            Pair(2, {
                AppBus.postDelay("0_0", AppBus.ACTION_BEGIN_SCREEN_PICKER, 800)
                finish()
            }),
            Pair(3, {
                DataCollector.buriedPoint("sa_4")
                shareScreen()
            }),
            Pair(4, {
                DataCollector.buriedPoint("sa_5")
                scanQrCode()
            }),
            Pair(5, {
                DataCollector.buriedPoint("sa_6")
                save2Local()
            })
    )

    private val itemClick: (Int) -> Unit = { pos ->
        funMap[pos]?.invoke()
    }

    private fun scanQrCode() {
        isReady ?: return
        showProgressBar = true
        QRTools.parseFile(screenPath) {
            runOnUi {
                showProgressBar = false
                onScanQRCodeSuccess(it)
            }
        }
    }

    private fun shareScreen() {
        isReady ?: return
        SystemBridge.shareImage(screenPath)
        finish()
    }

    private fun save2Local() {
        isReady ?: return

        showProgressBar = true
        runOnNewHandlerThread {
            try {
                val path = Environment.getExternalStorageDirectory().absolutePath +
                        "/Pictures/Screenshots/Screenshot_${formatNow("yyyyMMdd-HHmmss")}.jpg"

                val f = File(path)
                File(screenPath).copyTo(f, true)

                GlobalApp.APP.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(f)))
                GlobalApp.toastInfo("保存到 $path")
            } catch (e: SecurityException) {
                GlobalApp.toastError("保存失败：无存储权限")
            } catch (e: Exception) {
                GlobalApp.toastError("保存失败：${e.message}")
            }
            showProgressBar = false
        }
    }

    private fun finishIfNotShowing() {
        if (!bottomController.isBottomSheetShowing) {
            finish()
        }
    }

    private fun onScanQRCodeSuccess(result: String?) {
        Vog.d("onScanQRCodeSuccess ---> $result")
        if (result == null) {
            finishIfNotShowing()
            GlobalApp.toastError("无识别结果")
            return
        }

        AlertDialog.Builder(this).setTitle("识别结果")
                .setMessage(result)
                .setPositiveButton("复制") { _, _ -> SystemBridge.setClipText(result) }
                .setNegativeButton("分享") { _, _ -> SystemBridge.shareText(result) }
                .apply {
                    setOnDismissListener {
                        finishIfNotShowing()
                    }
                    when {
                        result.startsWith("http", ignoreCase = true) -> {
                            setNeutralButton("访问") { _, _ ->
                                finish()
                                SystemBridge.openUrl(result.substring(0, 5).toLowerCase() // 某些HTTP://
                                        + result.substring(5))
                            }
                        }
                        TextHelper.isEmail(result) -> {
                            setNeutralButton("发邮件") { _, _ ->
                                finish()
                                SystemBridge.sendEmail(result)
                            }
                        }
                        result.startsWith("market:", ignoreCase = true) -> {
                            setNeutralButton("打开应用市场") { _, _ ->
                                finish()
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse(result)
                                //跳转酷市场
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                        }
                        result.startsWith("smsto:", ignoreCase = true) -> {
                            setNeutralButton("发送短信") { _, _ ->
                                val ss = result.split(':')
                                val p = try {
                                    ss[1]
                                } catch (e: Exception) {
                                    GlobalApp.toastError("未发现手机号")
                                    return@setNeutralButton
                                }
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
                            setNeutralButton("拨号") { _, _ ->
                                finish()
                                SystemBridge.call(result.substring(4))
                            }
                        }
                    }
                    show()
                }

    }

    /**
     * 文字识别
     */
    private fun screenOcr() {
        isReady ?: return
        showProgressBar = true
        runOnNewHandlerThread {
            try {
                val intent = Intent(GlobalApp.APP,
                        TextOcrActivity::class.java).also {
                    val results = BaiduAipHelper.ocr(UtilBridge.compressImage(screenPath))
                    it.putExtra("items", results)
                }
                if (isFinishing) return@runOnNewHandlerThread
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                GlobalApp.toastError(e.message ?: "")
            } finally {
                showProgressBar = false
            }
        }
    }

    var dialog: Dialog? = null

    /**
     * 图像识别
     */
    private fun imageClassify() {
        isReady ?: return

        showProgressBar = true
        ThreadPool.runOnPool {
            val r = BaiduAipHelper.imageClassify(UtilBridge.compressImage(screenPath))
            runOnUi {
                showProgressBar = false
                Vog.d("imageClassify ---> ${r?.bestResult}")
                val result = r?.bestResult
                if (r?.hasErr == false && result != null) {
                    if (result.keyword == "屏幕截图") {
                        GlobalApp.toastError("无识别结果")
                        if (!bottomController.isBottomSheetShowing) {
                            finish()
                        }
                    } else if (!isFinishing) {
                        dialog = ImageClassifyResultDialog(result, this, screenPath) {
                            finish()
                        }.also { it.show() }
                    }
                } else {
                    GlobalApp.toastError("识别失败")
                    if (!bottomController.isBottomSheetShowing) {
                        finish()
                    }
                }
            }
        }
    }


}