package cn.vove7.common.app

import cn.vove7.jarvis.jadb.JAdb
import cn.vove7.vtp.runtimepermission.PermissionUtils
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * # AppPermission
 *
 * @author Vove
 * 2019/7/31
 */
object AppPermission {
    val canWriteSecureSettings: Boolean
        get() = PermissionUtils.isAllGranted(GlobalApp.APP,
                arrayOf("android.permission.WRITE_SECURE_SETTINGS"))


    fun autoOpenWriteSecureWithAdb(jadb:JAdb) {
        if (canWriteSecureSettings) {
            return
        }
        thread {
            kotlin.runCatching {
                jadb.execOnShell(" pm grant ${GlobalApp.APP.packageName} android.permission.WRITE_SECURE_SETTINGS")
                sleep(1000)
                jadb.close()
                GlobalLog.log("adb WRITE_SECURE_SETTINGS: $canWriteSecureSettings")
            }.onFailure {
                GlobalLog.err(it)
            }
        }

    }
}