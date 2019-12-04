package cn.vove7.jarvis.tools

import android.graphics.*
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import cn.vove7.common.bridges.UtilBridge
import cn.vove7.common.utils.StorageHelper
import cn.vove7.vtp.log.Vog
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.pow


/**
 * # QRTools
 *
 * @author Administrator
 * 2018/11/6
 */
object QRTools {

    fun parseBitmap(srcBitmap: Bitmap, onResult: (String?) -> Unit): Job = GlobalScope.launch {
        var result: String? = null
        val gray by lazy { srcBitmap.toGray() }
        try {

            result = decode(srcBitmap) ?: decode(gray)
            if (result != null) {
                return@launch
            }
            val minSize = 170
            var imgSize: Int = Math.min(gray.width, gray.getHeight())
            var level = 1
            while (imgSize > minSize) {
                val newImage: Bitmap = gray.scale(0.9.pow(level).toFloat())
                result = decode(newImage)
                if (result != null && result.isNotEmpty()) {
                    newImage.recycle()
                    return@launch
                }
                imgSize = newImage.width.coerceAtMost(newImage.height)
                newImage.recycle()
                level++
            }
        } finally {
            gray.recycle()
            withContext(Dispatchers.Main) {
                onResult(result)
            }

        }
    }

    private fun decode(bm: Bitmap): String? {
        val width: Int = bm.width
        val height: Int = bm.height
        val pixels = IntArray(width * height)
        bm.getPixels(pixels, 0, width, 0, 0, width, height)
        // 新建一个RGBLuminanceSource对象
        val source = RGBLuminanceSource(width, height, pixels)
        // 将图片转换成二进制图片 坑
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader() // 初始化解析对象

        val hints: MutableMap<DecodeHintType, Any> = HashMap()
        hints[DecodeHintType.CHARACTER_SET] = "UTF-8"
        return try {
            reader.decode(binaryBitmap, hints)?.text
        } catch (e: Throwable) {
            Vog.e(e)
            null
        }
    }

    fun parseFile(path: String, onResult: (String?) -> Unit): Job {
        val img = BitmapFactory.decodeFile(path)
        return parseBitmap(img) {
            img.recycle()
            onResult(it)
        }
    }

    /**
     * 将图片转成灰阶。
     *
     * @return
     */
    private fun Bitmap.toGray(): Bitmap {
        val bmpOriginal = this
        val bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val c = Canvas(bmpGray)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGray
    }

    private fun Bitmap.scale(scale: Float): Bitmap {
        val matrix = Matrix()
        matrix.postScale(scale, scale) // 使用后乘
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
    }

    fun encode(content: String, onFinish: (String?, e: Throwable?) -> Unit) = GlobalScope.launch {
        try {
            val hw = 400
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, hw, hw)
            val pixels = IntArray(hw * hw)
            for (y in 0 until hw) {
                for (x in 0 until hw) {
                    if (matrix[x, y]) {
                        pixels[y * hw + x] = BLACK
                    } else {
                        pixels[y * hw + x] = WHITE
                    }
                }
            }
            val bitmap = Bitmap.createBitmap(hw, hw, Bitmap.Config.RGB_565)
            bitmap.setPixels(pixels, 0, hw, 0, 0, hw, hw)

            if (bitmap != null) {
                val tmpFile = StorageHelper.cacheDir + "/qrtmp.png"
                UtilBridge.bitmap2File(bitmap, tmpFile)
                onFinish.invoke(tmpFile, null)
            } else onFinish.invoke(null, null)
        } catch (e: Throwable) {
            onFinish(null, e)
        }
    }
}
