package cn.vove7.common.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.model.ResultBox
import cn.vove7.common.utils.CoroutineExt.launch
import cn.vove7.common.view.finder.ViewFindBuilder.Companion.text

import cn.vove7.vtp.log.Vog

class ScreenshotActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 全屏截屏 状态栏收起?
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        Vog.d("onCreate --->")
        super.onCreate(savedInstanceState)

        requestCap()

    }

    var clickAuto = false
    private fun requestCap() {//请求
        val mMediaProjectionManager = application.getSystemService(Context.MEDIA_PROJECTION_SERVICE)
                as MediaProjectionManager
        try {
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
            //自动点击
            if (Build.VERSION.SDK_INT >= 29 && AccessibilityApi.isBaseServiceOn) {
                launch {
                    clickAuto = text("立即开始", "允许", "start now")
                            .waitFor(1000)?.tryClick() ?: false
                }
            }
        } catch (e: Exception) {
            GlobalApp.toastWarning("不支持截屏")
            GlobalLog.err(e)
            notifyResult()
        }
    }

    override fun finish() {
        super.finish()
        Vog.d("finish --->")
        overridePendingTransition(0, 0)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_MEDIA_PROJECTION -> {
                if (resultCode == RESULT_OK && data != null) {
                    notifyResult(data, clickAuto)
                } else {
                    notifyResult()
                    GlobalApp.toastError("无权限截屏")
                }
            }
        }
        finish()
    }

    companion object {

        private const val REQUEST_MEDIA_PROJECTION = 100
        var resultBox: ResultBox<Pair<Intent?, Boolean>>? = null

        fun notifyResult(b: Intent? = null, autoClick: Boolean = false) {
            Vog.d("notifyResult ---> $b")
            resultBox?.setAndNotify(b to autoClick)
            resultBox = null
        }

        fun getScreenshotIntent(context: Context, r: ResultBox<Pair<Intent?, Boolean>>): Intent {
            resultBox = r
            val intent = Intent(context, ScreenshotActivity::class.java)
            intent.flags = (
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                            or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                            or Intent.FLAG_FROM_BACKGROUND
                            or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            return intent
        }
    }

}
