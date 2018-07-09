package cn.vove7.vtp.dialog

import android.app.Dialog
import android.os.Build
import android.view.WindowManager
import cn.vove7.vtp.runtimepermission.PermissionUtils

/**
 *
 *
 * Created by Vove on 2018/7/2
 */
object DialogUtil {
    fun setFloat(dialog: Dialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtils.canDrawOverlays(dialog.context)) {
                throw Exception("无悬浮窗权限")
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        } else {
            dialog.window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        }
    }
}