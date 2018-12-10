package cn.vove7.common.utils

import android.os.Environment
import cn.vove7.common.app.GlobalLog
import java.io.File

/**
 * # StorageHelper
 *
 * @author Administrator
 * 2018/11/24
 */
object StorageHelper {
    val storePath by lazy {
        val p = Environment.getExternalStorageDirectory().absolutePath + "/V Assist"
        createDir(p)
        p
    }

    /**
     * 数据备份目录
     */
    val backupPath: String by lazy {
        val p = "$storePath/backup"
        createDir(p)
        p
    }

    /**
     * 插件下载目录
     */
    val pluginsPath: String by lazy {
        val p = "$storePath/plugins"
        createDir(p)
        p
    }

    /**
     * sp配置重定向目录
     */
    val spPath: String by lazy {
        val p = "$storePath/sp_config"
        createDir(p)
        p
    }


    private fun createDir(p: String) {
        File(p).apply {
            try {
                if (!exists()) {
                    mkdirs()
                }
            } catch (e: Exception) {
                GlobalLog.err("目录创建失败" + e.message, "sh39")
            }
        }
    }
}