package cn.vove7.jarvis.assist

import android.app.AlertDialog
import android.app.Dialog
import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.service.voice.VoiceInteractionSession
import android.support.annotation.RequiresApi
import android.support.design.widget.BottomSheetBehavior
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.AppBus.EVENT_BEGIN_RECO
import cn.vove7.common.appbus.AppBus.EVENT_ERROR_RECO
import cn.vove7.common.appbus.AppBus.EVENT_FINISH_RECO
import cn.vove7.common.appbus.AppBus.EVENT_HIDE_FLOAT

import cn.vove7.common.bridges.UtilBridge
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.R
import cn.vove7.jarvis.adapters.SimpleListAdapter
import cn.vove7.jarvis.adapters.ViewModel
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.view.BottomSheetController
import cn.vove7.vtp.log.Vog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.concurrent.thread
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus.ORDER_BEGIN_SCREEN_PICKER
import cn.vove7.common.baiduaip.BaiduAipHelper
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.runOnUi
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.tools.QRTools
import cn.vove7.jarvis.view.dialog.ImageClassifyResultDialog
import cn.vove7.vtp.dialog.DialogUtil
import java.lang.Thread.sleep
import android.graphics.Matrix


/**
 * # AssistSession
 * 会话界面
 */
@RequiresApi(api = Build.VERSION_CODES.M)
class AssistSession(context: Context) : VoiceInteractionSession(context),
        SimpleListAdapter.OnItemClickListener {
    lateinit var bottomSheetController: BottomSheetController
    private var pb: ProgressBar? = null
    private var screenshot: Bitmap? = null
    var screenPath: String? = null
    override fun onAssistStructureFailure(failure: Throwable) {
        failure.printStackTrace()
        Vog.d(this, "onAssistStructureFailure ---> ${failure.message}")
    }

    override fun onCreate() {
        super.onCreate()
        AppBus.reg(this)
    }

    override fun onHandleScreenshot(screenshot: Bitmap?) {
        if (screenshot == null) return
        screenPath = "loadding"
        showProgressBar = true
        HandlerThread("save_screen").apply {
            start()
            Handler(looper).post {
                val ss = compressMaterix(screenshot)
                this@AssistSession.screenshot = ss
                Vog.d(this, "onHandleScreenshot ---> $screenshot")
                screenPath = UtilBridge.bitmap2File(ss, context.cacheDir
                        .absolutePath + "/screen.png")?.absolutePath
                if (!UserInfo.isVip())
                    sleep(500)
                showProgressBar = false
                quitSafely()
            }
        }
    }

    override fun onBackPressed() {//ani
        bottomSheetController.hideBottom()
    }

    override fun onHandleAssist(data: Bundle?, structure: AssistStructure?, content: AssistContent?) {
//        AssistScreenContentDumpThread(context, data, structure, content).start()
        Vog.d(this, "onHandleAssist ---> onHandleAssist")
        if (AppConfig.recoWhenWakeupAssist)
            MainService.switchReco()
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        Vog.d(this, "onKeyLongPress ---> $keyCode")
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onCreateContentView(): View {
        val view = layoutInflater.inflate(R.layout.dialog_assist, null)
        pb = view.findViewById(R.id.progress_bar)
        showProgressBar = showProgressBar
        bottomSheetController = BottomSheetController(context, view.findViewById(R.id.bottom_sheet))
        bottomSheetController.setBottomListData(items, this)
        bottomSheetController.behavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(p0: View, p1: Int) {
                if (p1 == BottomSheetBehavior.STATE_HIDDEN) finish()
            }

            override fun onSlide(p0: View, p1: Float) {}
        })
        view.findViewById<View>(R.id.root).setOnClickListener { bottomSheetController.hideBottom() }
        return view
    }

    private var showProgressBar: Boolean = false
        set(value) {
            runOnUi {
                if (value) pb?.visibility = View.VISIBLE
                else pb?.visibility = View.INVISIBLE
            }
            field = value
        }

    override fun onClick(holder: SimpleListAdapter.VHolder?, pos: Int, item: ViewModel) {
        when (pos) {
            0 -> {
                val path = screenPath
                if (path == null) GlobalApp.toastShort("屏幕内容获取失败")
                else if (path == "loadding") GlobalApp.toastShort("等待加载完成")
                else imageClassify(path)
            }
            1 -> {
                AppBus.postDelay("0_0", ORDER_BEGIN_SCREEN_PICKER, 300)
                hide()
            }
            2 -> {
                SystemBridge.shareImage(screenPath)
                hide()
            }
            3 -> {
                when (screenPath) {
                    null -> GlobalApp.toastShort("屏幕内容获取失败")
                    "loadding" -> GlobalApp.toastShort("等待加载完成")
                    else -> {
                        showProgressBar = true
                        QRTools.parseBitmap(screenshot!!) {
                            runOnUi {
                                showProgressBar = false
                                onScanQRCodeSuccess(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private val items = mutableListOf(
            ViewModel("屏幕识别", icon = context.getDrawable(R.drawable.ic_screen_content)),
            ViewModel("文字提取", icon = context.getDrawable(R.drawable.ic_tt)),
            ViewModel("分享屏幕", icon = context.getDrawable(R.drawable.ic_screenshot)),
            ViewModel("二维码/条码识别", icon = context.getDrawable(R.drawable.ic_qr_code))
    )

    override fun onHide() {
        Vog.d(this, "onHide ---> ")
        AppBus.unreg(this)
        AppBus.post(AppBus.ORDER_CANCEL_RECO)
        dialog?.dismiss()
        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: String) {
        when (e) {
            EVENT_HIDE_FLOAT -> hide()
            EVENT_BEGIN_RECO -> {//开始识别

            }
            EVENT_FINISH_RECO -> {

            }
            EVENT_ERROR_RECO -> {

            }
            else -> {
            }
        }
    }

    private fun onScanQRCodeSuccess(result: String?) {
        Vog.d(this, "onScanQRCodeSuccess ---> $result")
        if (result == null) {
            GlobalApp.toastShort("无识别结果")
            return
        }

        AlertDialog.Builder(context).setTitle("识别结果")
                .setMessage(result)
                .setPositiveButton("复制") { _, _ -> SystemBridge.setClipText(result) }
                .setNegativeButton("分享") { _, _ -> SystemBridge.shareText(result) }
                .also {
                    if (result.startsWith("http"))
                        it.setNeutralButton("访问") { _, _ ->
                            hide()
                            SystemBridge.openUrl(result)
                        }
                    it.create().apply {
                        try {
                            DialogUtil.setFloat(this)//悬浮权限
                            show()
                        } catch (e: Exception) {
                            AppBus.post(RequestPermission("悬浮窗权限"))
                        }
                    }
                }

//        MaterialDialog(context).title(text = "识别结果")
//                .message(text = result)
//                .positiveButton(text = "复制") { SystemBridge.setClipText(result) }
//                .negativeButton(text = "分享") { SystemBridge.shareText(result) }
//                .show()
    }

    var dialog: Dialog? = null
    /**
     * 图像识别
     * @param path String
     */
    private fun imageClassify(path: String) {
        showProgressBar = true
        thread {
            val r = BaiduAipHelper.imageClassify(path)
            runOnUi {
                showProgressBar = false
                Vog.d(this, "imageClassify ---> ${r?.bestResult}")
                val result = r?.bestResult
                if (r?.hasErr == false && result != null) {
                    dialog = ImageClassifyResultDialog(result, context, screenshot).also { it.show() }
                } else {
                    GlobalApp.toastShort("识别失败")
                }
            }
        }
    }

    fun compressMaterix(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.setScale(0.5f, 0.5f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
