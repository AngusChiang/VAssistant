package cn.vove7.admin_manager

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.vtp.log.Vog


/**
 * # AdminReceiver
 *
 * @author Vove
 * 2019/7/21
 */
class AdminReceiver : DeviceAdminReceiver() {
    companion object {
        var instance: AdminReceiver? = null

        fun isActive(): Boolean {
            val app = GlobalApp.APP
            val policyManager = app.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager?

            return policyManager?.isAdminActive(ComponentName(app, AdminReceiver::class.java))?: false
        }
    }


    override fun onEnabled(context: Context, intent: Intent) {
        GlobalLog.log("设备管理器权限开启")
        instance = this

    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        GlobalLog.log("设备管理器权限关闭")
        instance = null
    }
}