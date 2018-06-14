package cn.vove7.vtp.runtimepermission

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.accessibility.AccessibilityManager
import cn.vove7.vtp.log.Vog

object PermissionUtils {

    /**
     * @return 无障碍服务是否开启
     */
    fun accessibilityServiceEnabled(context: Context): Boolean {
        val pkg = context.packageName
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledAccessibilityServiceList = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (info in enabledAccessibilityServiceList) {
            Log.d("#########", "all -->" + info.id)
            if (info.id.contains(pkg)) {
                return true
            }
        }

        return false
    }

    /**
     * @return 通知使用权限是否开启
     */
    fun notificationListenerEnabled(context: Context): Boolean {
        val enable: Boolean
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        Vog.v(this, "flat-" + flat!!)
        enable = flat.contains(packageName)
        return enable
    }

    /**
     * 跳转通知使用权限
     */
    fun gotoNotificationAccessSetting(context: Context): Boolean {
        try {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return true
        } catch (e: ActivityNotFoundException) {
            try {
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val cn = ComponentName("com.android.settings", "com.android.settings.Settings\$NotificationAccessSettingsActivity")
                intent.component = cn
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings")
                context.startActivity(intent)
                return true
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return false
        }
    }

    /**
     * 自动请求多个权限
     * requestCode 为权限数组index
     * 重写父类 onRequestPermissionsResult
     * @see AppCompatActivity#onRequestPermissionsResult()
     * @param requestCode default 1
     * @return 返回是否全部被授权
     *
     * help:
     *  PackageManager.PERMISSION_GRANTED 表示有权限，
     *  PackageManager.PERMISSION_DENIED 表示无权限。
     *
     */
    fun autoRequestPermission(context: Context, permissions: Array<String>, requestCode: Int = 1): Boolean {
        val noPermission = arrayListOf<String>()
        permissions.forEach {
            if (ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED)
                noPermission.add(it)
        }
        return if (noPermission.size > 0) {
            ActivityCompat.requestPermissions(context as Activity, noPermission.toTypedArray(), requestCode)
            false
        } else {
            true
        }
    }


    /**
     * @return 是否全部授权
     * 可用于查询必须权限是否全部被授权
     */
    fun isAllGranted(context: Context, permissions: Array<String>): Boolean {
        permissions.forEach {
            if (ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }

    /**
     * onRequestPermissionsResult返回结果
     * @return 是否全部授权
     */
    fun isAllGranted(grantResults: IntArray): Boolean {
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }
}
