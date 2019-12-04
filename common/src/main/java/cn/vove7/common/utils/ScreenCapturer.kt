package cn.vove7.common.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources.getSystem
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.RequiresApi


class ScreenCapturer(private val mContext: Context, data: Intent) {
    private val mCachedImageLock = Object()
    private val mProjectionManager: MediaProjectionManager =
        mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    private var mImageReader: ImageReader? = null
    private val mMediaProjection: MediaProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, data)
    private var mVirtualDisplay: VirtualDisplay? = null
    @Volatile
    private var mUnderUsingImage: Image? = null
    @Volatile
    private var mCachedImage: Image? = null
    @Volatile
    private var mImageAvailable = false
    @Volatile
    private var mException: Exception? = null

    init {
        createVirtualDisplay()
    }

    private fun createVirtualDisplay() {
        mImageReader?.close()
        mVirtualDisplay?.release()
        mImageAvailable = false
        //暂时不管 屏幕旋转 后刷新
        val wm = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val mDisplay = wm.defaultDisplay
        val metrics = DisplayMetrics()
        mDisplay.getRealMetrics(metrics)
        initVirtualDisplay(metrics.widthPixels, metrics.heightPixels)
        startAcquireImageLoop()
    }

    private fun initVirtualDisplay(width: Int, height: Int) {
        mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(LOG_TAG,
                width, height, getSystem().displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader!!.surface, null, null)
    }

    private fun startAcquireImageLoop() {
        setImageListener(Handler(Looper.getMainLooper()))
    }

    private fun setImageListener(handler: Handler) {
        mImageReader!!.setOnImageAvailableListener({ reader: ImageReader ->
            try {
                if (mCachedImage != null) {
                    synchronized(mCachedImageLock) {
                        mCachedImage?.close()
                        mCachedImage = reader.acquireLatestImage()
                        mImageAvailable = true
                        mCachedImageLock.notify()
                        return@setOnImageAvailableListener
                    }
                }
                mCachedImage = reader.acquireLatestImage()
            } catch (e: Exception) {
                mException = e
            }
        }, handler)
    }

    @Throws(Exception::class)
    fun capture(): Image? {
        if (!mImageAvailable) {
            waitForImageAvailable()
        }
        synchronized(mCachedImageLock) {
            if (mCachedImage != null) {
                if (mUnderUsingImage != null) mUnderUsingImage!!.close()
                mUnderUsingImage = mCachedImage
                mCachedImage = null
            }
        }
        mImageAvailable = false
        return mUnderUsingImage
    }

    @Throws(InterruptedException::class)
    private fun waitForImageAvailable() {
        synchronized(mCachedImageLock) {
            if (mImageAvailable) {
                return
            }
            mCachedImageLock.wait()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun release() {
        mMediaProjection.stop()
        mVirtualDisplay?.release()
        mImageReader?.close()
        mUnderUsingImage?.close()
        mCachedImage?.close()
    }

    protected fun finalize() {
        release()
    }

    companion object {
        private const val LOG_TAG = "ScreenCapturer"
    }

}