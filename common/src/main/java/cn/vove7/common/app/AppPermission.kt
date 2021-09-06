package cn.vove7.common.app

import cn.vove7.android.common.logi
import cn.vove7.jadb.AdbClient
import cn.vove7.vtp.runtimepermission.PermissionUtils
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


    fun autoOpenWriteSecureWithAdb(jadb: AdbClient) {
        if (canWriteSecureSettings) {
            return
        }
        thread {
            kotlin.runCatching {
                val s = jadb.shellCommand("pm grant ${GlobalApp.APP.packageName} android.permission.WRITE_SECURE_SETTINGS")
                s.onClose {
                    "autoOpenWriteSecureWithAdb 执行结束 ${String(data)}".logi()
                    s.close()
                }
                GlobalLog.log("adb WRITE_SECURE_SETTINGS: $canWriteSecureSettings")
            }.onFailure {
                GlobalLog.err(it)
            }
        }
    }
}