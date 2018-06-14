package cn.vove7.vtp.asset

import android.content.Context
import java.io.IOException

/**
 * Asset类
 */
object AssetUtil {

    fun getStrFromAsset(context: Context, fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val bs = ByteArray(inputStream.available())
            inputStream.read(bs)
            bs.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}