package cn.vove7.common.utils

import android.os.Environment

/**
 * # StorageHelper
 *
 * @author Administrator
 * 2018/11/24
 */
object StorageHelper {
    val storePath by lazy { Environment.getExternalStorageDirectory().absolutePath + "/V Assist" }

    val backupPath: String by lazy { "$storePath/backup" }

    /**
     * 插件下载目录
     */
    val pliginsPath: String by lazy { "$storePath/plugins" }

}