package cn.vove7.jarvis.tools

import android.graphics.Bitmap
import com.google.zxing.*
import kotlin.concurrent.thread
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer


/**
 * # QRTools
 *
 * @author Administrator
 * 2018/11/6
 */
object QRTools {
    fun parseBitmap(bitmap: Bitmap, onResult: (String?) -> Unit) {
        thread {
            val s = syncDecodeQRCode(bitmap)
            onResult.invoke(s)
        }

    }

    fun syncDecodeQRCode(bitmap: Bitmap): String? {
        var result: Result?
        var source: RGBLuminanceSource? = null
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            source = RGBLuminanceSource(width, height, pixels)
            result = MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source)), HINTS)
            result?.text
        } catch (e: Exception) {
            e.printStackTrace()
            if (source != null) {
                try {
                    result = MultiFormatReader().decode(BinaryBitmap(GlobalHistogramBinarizer(source)), HINTS)
                    return result?.text
                } catch (e2: Throwable) {
                    e2.printStackTrace()
                }
            }
            null
        }

    }

    private val HINTS: Map<DecodeHintType, Any>
        get() {
            val allFormats = ArrayList<BarcodeFormat>()
            allFormats.add(BarcodeFormat.AZTEC)
            allFormats.add(BarcodeFormat.CODABAR)
            allFormats.add(BarcodeFormat.CODE_39)
            allFormats.add(BarcodeFormat.CODE_93)
            allFormats.add(BarcodeFormat.CODE_128)
            allFormats.add(BarcodeFormat.DATA_MATRIX)
            allFormats.add(BarcodeFormat.EAN_8)
            allFormats.add(BarcodeFormat.EAN_13)
            allFormats.add(BarcodeFormat.ITF)
            allFormats.add(BarcodeFormat.MAXICODE)
            allFormats.add(BarcodeFormat.PDF_417)
            allFormats.add(BarcodeFormat.QR_CODE)
            allFormats.add(BarcodeFormat.RSS_14)
            allFormats.add(BarcodeFormat.RSS_EXPANDED)
            allFormats.add(BarcodeFormat.UPC_A)
            allFormats.add(BarcodeFormat.UPC_E)
            allFormats.add(BarcodeFormat.UPC_EAN_EXTENSION)
            val map = mutableMapOf<DecodeHintType, Any>()
            map[DecodeHintType.TRY_HARDER] = BarcodeFormat.QR_CODE
            map[DecodeHintType.POSSIBLE_FORMATS] = allFormats
            map[DecodeHintType.CHARACTER_SET] = "utf-8"
            return map
        }

}