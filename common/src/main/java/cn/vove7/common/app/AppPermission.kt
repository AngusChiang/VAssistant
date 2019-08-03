package cn.vove7.common.app

import cn.vove7.vtp.runtimepermission.PermissionUtils

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

}