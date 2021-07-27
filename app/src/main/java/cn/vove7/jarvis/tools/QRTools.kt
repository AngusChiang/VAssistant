package cn.vove7.jarvis.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import cn.vove7.common.bridges.UtilBridge
import cn.vove7.common.utils.StorageHelper
import com.king.zxing.util.CodeUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


/**
 * # QRTools
 *
 * @author Administrator
 * 2018/11/6
 */
object QRTools {

    fun parseBitmap(srcBitmap: Bitmap, onResult: (String?) -> Unit): Job = GlobalScope.launch {
        onResult(CodeUtils.parseCode(srcBitmap))
    }

    fun parseFile(path: String, onResult: (String?) -> Unit): Job {
        val img = BitmapFactory.decodeFile(path)
        return parseBitmap(img) {
            img.recycle()
            onResult(it)
        }
    }

    fun encode(content: String, onFinish: (String?, e: Throwable?) -> Unit) = GlobalScope.launch {
        try {
            val bm = CodeUtils.createQRCode(content, 400, Color.BLACK)
            if (bm != null) {
                val tmpFile = StorageHelper.cacheDir + "/qrtmp.png"
                UtilBridge.bitmap2File(bm, tmpFile)
                onFinish.invoke(tmpFile, null)
            } else onFinish.invoke(null, null)
        } catch (e: Throwable) {
            onFinish(null, e)
        }
    }
}
