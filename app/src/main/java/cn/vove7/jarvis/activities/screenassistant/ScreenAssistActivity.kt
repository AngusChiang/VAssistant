package cn.vove7.jarvis.activities.screenassistant

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION
import android.view.animation.AlphaAnimation
import android.widget.PopupMenu
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.bridges.UtilBridge
import cn.vove7.common.utils.*
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.TextOcrActivity
import cn.vove7.jarvis.activities.base.BaseActivity
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.*
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.view.bottomsheet.AssistSessionGridController
import cn.vove7.jarvis.view.dialog.ImageClassifyResultDialog
import cn.vove7.vtp.log.Vog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.dialog_assist.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/**
 * # ScreenAssistActivity
 * 屏幕助手Activity
 * @author 11324
 * 2019/3/21
 */

val Bitmap.statusBarIsLight: Boolean
    get() {
        val spColor = getPixel(width / 2, 0)
        val r = Color.red(spColor)
        val g = Color.green(spColor)
        val b = Color.blue(spColor)
        return r + b + g > 450
    }

val Bitmap.navBarIsLight: Boolean
    get() {
        val spColor = getPixel(width / 2, height)
        val r = Color.red(spColor)
        val g = Color.green(spColor)
        val b = Color.blue(spColor)
        return r + b + g > 450
    }

class ScreenAssistActivity : BaseActivity() {

    /**
     * 截图文件在下次进入删除
     */
    private lateinit var screenPath: String
    private lateinit var bottomController: AssistSessionGridController

    companion object {

        fun createIntent(path: String? = null, delayCapture: Boolean = false, light: Boolean? = null): Intent {
            return Intent(GlobalApp.APP.packageName + ".SCREEN_ASSIST").apply {
                newTask()
                path?.also { putExtra("path", it) }
                light?.also { putExtra("light", it) }
                putExtra("delay", delayCapture)
            }
        }
    }

    private val isReady: Boolean
        get() = !showProgressBar

    private var showProgressBar: Boolean = false
        set(value) {
            field = value
            runOnUi {
                if (value) progress_bar.visibility = View.VISIBLE
                else progress_bar.visibility = View.INVISIBLE
            }
        }

    private fun hideNavBar() = runOnUi {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or window.decorView.systemUiVisibility
    }

    private fun setStatusBarLight() = runOnUi {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private var oneJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
            value?.invokeOnCompletion {
                oneJob = null
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setWindowAnimations(R.style.ScreenAssist)
        //from 屏幕助手
        if ("light" in intent) {
            hideNavBar()
            if (intent["light", false]) {
                setStatusBarLight()
            }
        }
        setContentView(R.layout.dialog_assist)
        DataCollector.buriedPoint("sa_0")

        showProgressBar = true
        bottomController = AssistSessionGridController(this, bottom_sheet, itemClick, ::onLongClick) {
            if (isReady) screenPath else null
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
        val p = root.parent
        if (p is View) {
            p.setOnClickListener {
                onBackPressed()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        oneJob?.cancel()
    }

    private fun showView() = runOnUi {
        if (!bottomController.isBottomSheetShowing) {
            bottomController.bottomView.visibility = View.VISIBLE
            bottomController.expandSheet()

            val animation = AlphaAnimation(0f, 1f)
            animation.duration = 300
            bottomController.bottomView.startAnimation(animation)
            bottomController.bottomView.post {
                val list = arrayListOf<View>()
                val list2 = arrayListOf<View>()
                bottomController.bottomView.findViewsWithText(list, "二维码/条码识别", FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
                bottomController.bottomView.findViewsWithText(list2, "屏幕识别", FIND_VIEWS_WITH_CONTENT_DESCRIPTION)

                Tutorials.oneStep(this, arrayOf(
                        ItemWrap(Tutorials.screen_assistant_spot, list2[0], "长按更多功能", "淘宝商品识别"),
                        ItemWrap(Tutorials.screen_assistant_qrcode, list[0], "长按查看更多功能", "使用微信扫一扫\n支付宝扫一扫")
                ))

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
        launch {
            cacheDir.listFiles()?.filter { it.isFile && it.extension == "png" && it.absolutePath != screenPath }?.forEach {
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
                val isDelay = getBooleanExtra("delay", false)
                launch {
                    delay(if (isDelay) 1000 else 0)
                    val path = SystemBridge.screenShot()?.let {
                        //截完图显示面板
                        if (it.statusBarIsLight) {
                            setStatusBarLight()
                        }
                        hideNavBar()
                        showView()
                        val p = UtilBridge.bitmap2File(it, cachePath)?.absolutePath
                        it.recycle()
                        p
                    }
                    SystemBridge.release()
                    if (path == null) {
                        GlobalApp.toastError("截图失败")
                        finish()
                        return@launch
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
                AppBus.postDelay(AppBus.ACTION_BEGIN_SCREEN_PICKER, 800)
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

    private fun onLongClick(item: AssistSessionGridController.SessionFunItem, v: View): Boolean {
        var handle = true
        when (item.name) {
            "二维码/条码识别" -> {
                popQrMenu(v)
            }
            "文字识别" -> {
                popOcrMenu(v)
            }
            "屏幕识别" -> {
                popSpotMenu(v)
            }
            else -> handle = false
        }
        return handle
    }

    private fun popOcrMenu(v: View) {
        PopupMenu(this, v, Gravity.END or Gravity.TOP).apply {
            inflate(R.menu.menu_ocr_action)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.item_ocr_with_bm -> {
                        if (ActionHelper.ocrWithBaiMiao(screenPath)) {
                            finish()
                        }
                    }
                }
                true
            }


        }.show()
    }

    private fun popSpotMenu(v: View) {
        PopupMenu(this, v, Gravity.END or Gravity.TOP).apply {
            inflate(R.menu.menu_spot_action)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.item_spot_with_taobao -> {
                        if (ActionHelper.spotWithTaobao(screenPath)) {
                            finish()
                        }
                    }
                    R.id.item_spot_with_jd -> {
                        if (ActionHelper.spotWithJD(screenPath)) {
                            finish()
                        }
                    }
                }
                true
            }
            show()
        }
    }

    private fun popQrMenu(v: View) {
        PopupMenu(this, v, Gravity.END or Gravity.TOP).apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.item_wechat_qr -> {
                        finish()
                        ActionHelper.qrWithWechat(screenPath)
                    }
                    R.id.item_alipay_qr -> {
                        if (ActionHelper.qrWithAlipay(screenPath)) {
                            finish()
                        }
                    }
                }
                true
            }
            inflate(R.menu.menu_qrcode_action)
            show()
        }

    }


    private val itemClick: (Int) -> Unit = { pos ->
        funMap[pos]?.invoke()
    }

    private fun scanQrCode() {
        if (!isReady) {
            intent?.putExtra("fun_id", 4)
            return
        }
        showProgressBar = true
        oneJob = QRTools.parseFile(screenPath) {
            runOnUi {
                showProgressBar = false
                onScanQRCodeResult(it)
            }
        }
    }

    private fun shareScreen() {
        if (!isReady) {
            intent?.putExtra("fun_id", 3)
            return
        }
        SystemBridge.shareImage(screenPath)
        finish()
    }

    private fun save2Local() {
        if (!isReady) {
            intent?.putExtra("fun_id", 5)
            return
        }
        showProgressBar = true
        runOnNewHandlerThread {
            try {
                val f = File(StorageHelper.screenshotsPath,
                        "Screenshot_${formatNow("yyyyMMdd-HHmmss")}.jpg")

                File(screenPath).copyTo(f, true)
                f.broadcastImageFile()
                GlobalApp.toastInfo("已保存")
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

    private fun onScanQRCodeResult(result: String?) {
        Vog.d("onScanQRCodeResult ---> $result")
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
                        result.startsWith("http", ignoreCase = true)
                                || result.matches(".*?://.*".toRegex()) -> {
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
        if (!isReady) {
            intent?.putExtra("fun_id", 1)
            return
        }
        showProgressBar = true
        oneJob = launch {
            try {
                val results = BaiduAipHelper.ocr(UtilBridge.compressImage(screenPath))
                if (oneJob?.isCancelled == false) {
                    TextOcrActivity.start(this@ScreenAssistActivity, results, intent.extras)
                    finish()
                }
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
        if (!isReady) {
            intent?.putExtra("fun_id", 0)
            return
        }

        showProgressBar = true
        launchIo {
            val r = BaiduAipHelper.imageClassify(UtilBridge.compressImage(screenPath))
            withContext(Dispatchers.Main) {
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
                        dialog = ImageClassifyResultDialog(result, this@ScreenAssistActivity, screenPath) {
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