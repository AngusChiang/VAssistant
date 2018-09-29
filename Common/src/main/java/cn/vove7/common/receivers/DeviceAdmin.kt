package cn.vove7.common.receivers

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import cn.vove7.common.app.GlobalApp

/**
 * # DeviceAdmin
 *
 * @author Administrator
 * 9/27/2018
 */
//class DeviceAdmin : DeviceAdminReceiver() {
//    companion object {
//        var enable: Boolean = false
//            get() {
//                    if (!field) {
//                        val mAdminName = ComponentName(GlobalApp.APP.packageName, DeviceAdmin::class.java.name)
//                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName)
//                        GlobalApp.APP.startActivity(intent)
//                }
//                return field
//            }
//    }
//
//    override fun onEnabled(context: Context?, intent: Intent?) {
//        enable = true
//        super.onEnabled(context, intent)
//    }
//
//    override fun onDisabled(context: Context?, intent: Intent?) {
//        enable = false
//        super.onDisabled(context, intent)
//    }
//
//    override fun onDisableRequested(context: Context?, intent: Intent?): CharSequence {
//        return "关闭后将无法使用锁屏功能"
//    }
//}