package cn.vove7.jarvis.activities.screenassistant

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.BottomSheetBehavior
import android.view.View
import android.view.animation.AlphaAnimation
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.utils.*
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.TextOcrActivity
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.QRTools
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.view.bottomsheet.AssistSessionGridController
import cn.vove7.jarvis.view.dialog.ImageClassifyResultDialog
import cn.vove7.vtp.dialog.DialogUtil
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
class ScreenAssistActivity : Activity() {

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

    private var showProgressBar: Boolean = false
        set(value) {
            runOnUi {
                if (value) progress_bar.visibility = View.VISIBLE
                else progress_bar.visibility = View.INVISIBLE
            }
            field = value
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_assist)
        showProgressBar = true
        bottomController = AssistSessionGridController(this, bottom_sheet, itemClick)
        bottomController.initView()
        bottomController.hideBottom()
        bottomController.bottomView.visibility = View.GONE

        handlerScreen()
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
        root.setOnClickListener {
            onBackPressed()
        }
    }


    private fun showView() {
        runOnUi {
            bottomController.bottomView.visibility = View.VISIBLE
            bottomController.showBottom()

            val animation = AlphaAnimation(0f, 1f)
            animation.duration = 500
            bottomController.bottomView.startAnimation(animation)
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
                    val path = SystemBridge.screen2File(cachePath)?.absolutePath

                    if (path == null) {
                        GlobalApp.toastError("截图失败")
                        finish()
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

    private val itemClick: (Int) -> Unit = { pos ->
        when (pos) {
            0 -> {
                imageClassify(screenPath)
            }
            1 -> {
                showProgressBar = true
                runOnNewHandlerThread {
                    try {
                        val intent = Intent(GlobalApp.APP,
                                TextOcrActivity::class.java).also {
                            val results = BaiduAipHelper.ocr(screenPath)
                            it.putExtra("items", results)
                        }
                        if (isFinishing) return@runOnNewHandlerThread
                        startActivity(intent)
                        showProgressBar = false
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        GlobalApp.toastInfo(e.message!!)
                    }

                }
            }
            2 -> {
                AppBus.postDelay("0_0", AppBus.ORDER_BEGIN_SCREEN_PICKER, 800)
                finish()
            }
            3 -> {
                SystemBridge.shareImage(screenPath)
                finish()
            }
            4 -> {
                showProgressBar = true
                QRTools.parseFile(screenPath) {
                    runOnUi {
                        showProgressBar = false
                        onScanQRCodeSuccess(it)
                    }
                }
            }
            5 -> {
                showProgressBar = true
                runOnNewHandlerThread {
                    try {
                        val path = Environment.getExternalStorageDirectory().absolutePath +
                                "/Pictures/Screenshots/Screenshot_${formatNow("yyyyMMdd-HHmmss")}.jpg"

                        File(screenPath).copyTo(File(path), true)
                        GlobalApp.toastInfo("保存到 $path")
                    } catch (e: SecurityException) {
                        GlobalApp.toastError("保存失败：无存储权限")
                    } catch (e: Exception) {
                        GlobalApp.toastError("保存失败：${e.message}")
                    }
                    showProgressBar = false
                }
            }
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
                    create().apply {
                        try {
                            DialogUtil.setFloat(this)//悬浮权限
                            show()
                        } catch (e: Exception) {
                            AppBus.post(RequestPermission("悬浮窗权限"))
                        }
                    }
                }

    }

    var dialog: Dialog? = null
    /**
     * 图像识别
     * @param path String
     */
    private fun imageClassify(path: String) {
        showProgressBar = true
        ThreadPool.runOnPool {
            val r = BaiduAipHelper.imageClassify(path)
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
                    } else {
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