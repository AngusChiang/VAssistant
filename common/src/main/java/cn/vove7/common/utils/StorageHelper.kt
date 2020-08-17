package cn.vove7.common.utils

import android.os.Environment
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import java.io.File

/**
 * # StorageHelper
 *
 * @author Administrator
 * 2018/11/24
 */
object StorageHelper {
    val extPath get() = GlobalApp.APP.getExternalFilesDir(null)!!.absolutePath

    val storePath: String
        get() = extPath.also { createDir(it) }

    val cacheDir: String get() = GlobalApp.APP.cacheDir.absolutePath

    /**
     * 数据备份目录
     */
    val backupPath: String get() = "$storePath/backup".also { createDir(it) }

    /**
     * 插件下载目录
     */
    val pluginsPath: String get() = "$storePath/plugins".also { createDir(it) }

    val logPath: String get() = "$storePath/log".also { createDir(it) }

    val picturesPath: String get() = "${Environment.getExternalStorageDirectory().absolutePath}/Pictures".also { createDir(it) }

    val screenshotsPath: String get() = "$picturesPath/Screenshots".also { createDir(it) }

    /**
     * sp配置重定向目录
     */
    val spPath: String get() = "$storePath/sp_config".also { createDir(it) }

    /**
     * 离线资源路径
     */
    val offlineResPath: String get() = "$storePath/off_res".also { createDir(it) }

    private fun createDir(p: String) {
        File(p).apply {
            try {
                if (!exists()) {
                    mkdirs()
                }
            } catch (e: Exception) {
                GlobalLog.err("目录创建失败" + e.message)
            }
        }
    }
}