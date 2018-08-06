package cn.vove7.jarvis.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.support.annotation.CallSuper
import android.view.WindowManager
import cn.vove7.appbus.AppBus
import cn.vove7.common.bridges.ChoiceData
import cn.vove7.common.model.RequestPermission
import cn.vove7.vtp.dialog.DialogWithList
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.runtimepermission.PermissionUtils

/**
 *
 * 选择框
 * Created by Vove on 2018/6/21
 */
open class BaseChoiceDialog<V : BaseListAdapter.ViewHolder>
(val context: Context, val title: String, val adapter: BaseListAdapter<V, ChoiceData>) {
    var dialog: DialogWithList = DialogWithList(context, adapter)
    @CallSuper
    open fun show(): Dialog {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtils.canDrawOverlays(context)) {
                AppBus.post(RequestPermission("悬浮窗权限"))
                throw Exception("无悬浮窗权限")
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        } else {
            dialog.window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        }
        dialog.setTitle(title)
        return dialog
    }
}
