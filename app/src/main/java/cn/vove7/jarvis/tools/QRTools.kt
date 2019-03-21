package cn.vove7.jarvis.tools

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.AttributeSet
import android.view.LayoutInflater
import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil
import cn.bingoogolapple.qrcode.zbar.ZBarView
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.R
import java.lang.Thread.sleep
import java.lang.ref.WeakReference


/**
 * # QRTools
 *
 * @author Administrator
 * 2018/11/6
 */
object QRTools {

    fun parseBitmap(bitmap: Bitmap, onResult: (String?) -> Unit) {
        val av = LayoutInflater.from(GlobalApp.APP).inflate(R.layout.zbar, null)
        val a = av.findViewById<MyBarView>(R.id.zbar)

        ScanTask(bitmap, a, onResult).perform()
        a.decodeQRCode(bitmap)
    }

    fun parseFile(path: String, onResult: (String?) -> Unit) {
        val av = LayoutInflater.from(GlobalApp.APP).inflate(R.layout.zbar, null)
        val a = av.findViewById<MyBarView>(R.id.zbar)
        ScanTask(path, a, onResult).perform()
        a.decodeQRCode(path)
    }
}

/**
 * 改写Zbar
 * @property mPicturePath String?
 * @property mBitmap Bitmap?
 * @property mQRCodeViewRef WeakReference<MyBarView>?
 * @property onResult Function1<String?, Unit>
 */
class ScanTask : AsyncTask<Void, Void, String?> {
    private var mPicturePath: String? = null
    private var mBitmap: Bitmap? = null
    private var mQRCodeViewRef: WeakReference<MyBarView>? = null
    private var onResult: (String?) -> Unit

    constructor(picturePath: String, qrCodeView: MyBarView, onResult: (String?) -> Unit) {
        mPicturePath = picturePath
        mQRCodeViewRef = WeakReference(qrCodeView)
        this.onResult = onResult
    }

    constructor(bitmap: Bitmap, qrCodeView: MyBarView, onResult: (String?) -> Unit) {
        mBitmap = bitmap
        mQRCodeViewRef = WeakReference(qrCodeView)
        this.onResult = onResult
    }

    fun perform(): ScanTask {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        return this
    }

    fun cancelTask() {
        if (status != AsyncTask.Status.FINISHED) {
            cancel(true)
        }
    }

    override fun onCancelled() {
        super.onCancelled()
        mQRCodeViewRef!!.clear()
        mBitmap = null
    }

    override fun doInBackground(vararg params: Void): String? {
        val qrCodeView = mQRCodeViewRef!!.get() ?: return null
        sleep(300)
        if (mPicturePath != null) {
            return qrCodeView.process(BGAQRCodeUtil.getDecodeAbleBitmap(mPicturePath))
        } else if (mBitmap != null) {
            val result = qrCodeView.process(mBitmap!!)
            mBitmap = null
            return result
        }
        return null
    }

    override fun onPostExecute(result: String?) {
        onResult.invoke(result)
    }
}

class MyBarView(context: Context?, attributeSet: AttributeSet?)
    : ZBarView(context, attributeSet) {

    fun process(bitmap: Bitmap): String? {
        val r = processBitmapData(bitmap)
        return try {
            r?.javaClass?.getDeclaredField("result")?.let {
                it.isAccessible = true
                it.get(r) as String?
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }
}