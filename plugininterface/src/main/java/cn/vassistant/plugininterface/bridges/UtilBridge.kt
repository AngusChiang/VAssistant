package cn.vassistant.plugininterface.bridges

import android.graphics.Bitmap
import cn.vassistant.plugininterface.app.GlobalApp
import cn.vassistant.plugininterface.app.GlobalLog
import cn.vove7.vtp.log.Vog
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
        Vog.d(this, "bitmap2File ---> $fullPath")
        return try {
            File(fullPath).apply {
                if (!parentFile.exists())
                    parentFile.mkdirs()
                FileOutputStream(this).use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
                }
            }
        } catch (se: SecurityException) {
            GlobalApp.toastShort("无存储权限")
            null
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalLog.err("bitmap2File 保存到失败")
            null
        }
    }

}