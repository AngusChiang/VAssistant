package cn.vove7.jarvis.view.dialog

import android.app.AlertDialog

import android.content.Context
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.model.RequestPermission
import cn.vove7.vtp.dialog.DialogUtil

/**
 * 全局对话框
 * Created by Vove on 2018/7/1
 */
open class FloatAlertDialog(context: Context) : AlertDialog(context) {
    override fun show() {
        try {
            DialogUtil.setFloat(this)
            super.show()
        } catch (e: Exception) {
            e.printStackTrace()
            AppBus.post(RequestPermission("悬浮窗权限"))
        }
    }
}