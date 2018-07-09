package cn.vove7.jarvis.view.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.WindowManager
import cn.vove7.vtp.runtimepermission.PermissionUtils

/**
 *
 *
 * Created by Vove on 2018/7/1
 */
class FloatAlertDialog(context: Context):AlertDialog(context) {
    override fun show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtils.canDrawOverlays(context)) {
                throw Exception("无悬浮窗权限")
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        } else {
            window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        }
        super.show()
    }
}