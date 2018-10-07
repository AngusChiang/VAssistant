package cn.vove7.common.bridges

import android.graphics.Bitmap
import cn.vove7.common.app.GlobalLog
import java.io.File
import java.io.FileOutputStream

/**
 * # UtilBridge
 *
 * @author Administrator
 * 2018/10/6
 */
object UtilBridge {

    fun bitmap2File(bitmap: Bitmap, fullPath: String): File? {//保存到本地
        return try {
            val f = File(fullPath)
            if (!f.parentFile.exists())
                f.parentFile.mkdirs()

            FileOutputStream(f).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
            }
            f
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalLog.err("bitmap2File 保存到失败")
            null
        }
    }

}