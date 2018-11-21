package cn.vove7.common.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.model.ResultBox
import cn.vove7.common.view.toast.ColorfulToast
import cn.vove7.vtp.log.Vog
import java.lang.Thread.sleep

class ScreenshotActivity : Activity() {
    private lateinit var mMediaProjection: MediaProjection
    private lateinit var mMediaProjectionManager: MediaProjectionManager
    private lateinit var mImageReader: ImageReader
    private lateinit var mVirtualDisplay: VirtualDisplay

    private val pic: Bitmap?
        get() {
            var b: Bitmap? = null
            sleep(800)//must wait status bar hide
            mImageReader.acquireLatestImage()?.use { image ->
//                Vog.d(this, "cap ---> from mImageReader")
                val width = image.width
                val height = image.height
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * width
                var bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride,
                        height, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(buffer)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                b = bitmap
            }
            if (b != null) {
                val img = ImageView(this)
                img.setImageBitmap(b)
                setContentView(img)
                img.post {//fixme 动画
                    val animY = ObjectAnimator.ofFloat(img, "scaleY",
                            1f, 0.7f)
                    val animX = ObjectAnimator.ofFloat(img, "scaleX",
                            1f, 0.7f)
                    val aSet = AnimatorSet()
                    aSet.play(animX).with(animY)
                    aSet.duration = 300
                    aSet.start()
                }
//                img.animate().scaleX(0.7f)
//                        .scaleY(0.7f)
//                        .setDuration(300).start()
            }
            return b
        }

    private val screenWidth: Int
        get() = Resources.getSystem().displayMetrics.widthPixels

    private val screenHeight: Int
        get() = Resources.getSystem().displayMetrics.heightPixels


    override fun onCreate(savedInstanceState: Bundle?) {
        // 全屏截屏 状态栏收起?
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)

        requestCap()
    }

    private fun requestCap() {//请求
        mMediaProjectionManager = application.getSystemService(Context.MEDIA_PROJECTION_SERVICE)
                as MediaProjectionManager
        try {
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
        } catch (e: Exception) {
            GlobalApp.toastShort("不支持截屏")
            GlobalLog.err(e)
            notifyShot(null)
        }
    }

    private fun startCapture() {
        setVirtualDisplay()
        val screenImg = pic
        stopVirtualDisplay()
        notifyShot(screenImg)
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    private fun setVirtualDisplay() {
        mImageReader = ImageReader.newInstance(screenWidth, screenHeight,
                PixelFormat.RGBA_8888, 1)

        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                screenWidth, screenHeight,
                Resources.getSystem().displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.surface, null, null)

    }

    private fun stopVirtualDisplay() {
        Vog.d(this, "stopVirtualDisplay ---> release mVirtualDisplay ")
        mImageReader.close()
        mVirtualDisplay.release()
        mMediaProjection.stop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_MEDIA_PROJECTION -> {
                if (resultCode == RESULT_OK && data != null) {
                    mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data)
                    startCapture()
                } else {
                    notifyShot(null)
                    ColorfulToast(this).red().showShort("无权限截屏")
                    finish()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        private const val REQUEST_MEDIA_PROJECTION = 100
        var resultBox: ResultBox<Bitmap?>? = null

        fun notifyShot(bitmap: Bitmap?) {
            resultBox?.setAndNotify(bitmap)
            resultBox = null
        }

        fun getScreenshotIntent(context: Context, r: ResultBox<Bitmap?>): Intent {
            resultBox = r
            val intent = Intent(context, ScreenshotActivity::class.java)
            intent.flags = (
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                            or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                            or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            return intent
        }
    }

}
