package cn.vove7.accessibilityservicedemo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.support.annotation.CallSuper
import android.view.WindowManager
import cn.vove7.executorengine.bridge.ChoiceData
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
        dialog.setWidth(0.8)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtils.canDrawOverlays(context)) {
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
