package cn.vove7.jarvis.activities

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.BaseAdapter
import android.widget.TextView
import cn.vove7.jarvis.R
import cn.vove7.vtp.easyadapter.BaseListAdapter
import cn.vove7.vtp.runtimepermission.PermissionUtils
import cn.vove7.vtp.toast.Voast
import kotlinx.android.synthetic.main.activity_permission_manager.*

/**
 * 权限管理
 */
class PermissionManagerActivity : AppCompatActivity() {

    lateinit var permissionList: List<PermissionStatus>
    lateinit var toast: Voast
    lateinit var adapter: BaseAdapter
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_manager)
        toast = Voast.with(this).top()
        //执行时消息
        if (intent.hasExtra("pName")) {
            toast.showShort(String.format(getString(R.string.text_operation_need_permission)
                    , intent.getStringExtra("pName")))

        }

        permissionList = PermissionStatus.refreshStatus(this)
        adapter = buildAdapter()
        list.adapter = adapter
    }

    private fun buildAdapter(): BaseAdapter {
        return object : BaseListAdapter<Holder, PermissionStatus>(this, permissionList) {

            override fun onCreateViewHolder(view: View): Holder = Holder(view)
            override fun layoutId(): Int = R.layout.item_of_permission_list

            @TargetApi(Build.VERSION_CODES.M)
            override fun onBindView(holder: Holder, pos: Int, item: PermissionStatus) {
                holder.title.text = item.permissionName
                holder.subtitle.text = item.desc
                when (item.isOpen) {
                    true -> {
                        holder.open.text = getString(R.string.text_opened)
                        holder.open.setTextColor(resources.getColor(R.color.status_green))
                        holder.title.setTextColor(resources.getColor(R.color.primary_text))
                    }
                    else -> {
                        holder.open.text = getString(R.string.text_to_open)
                        holder.open.setTextColor(resources.getColor(R.color.red_500))
                        holder.title.setTextColor(resources.getColor(R.color.red_500))
                    }
                }
                holder.itemView.setOnClickListener {
                    if (!item.isOpen) {
                        when {
                            item.permissionName == "悬浮窗" ->
                                PermissionUtils.requestDrawOverlays(this@PermissionManagerActivity, 0)
                            item.permissionName == "无障碍" ->
                                PermissionUtils.gotoAccessibilitySetting(this@PermissionManagerActivity)
                            else ->
                                PermissionUtils.autoRequestPermission(this@PermissionManagerActivity,
                                        arrayOf(item.permissionString),
                                        pos)
                        }
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        PermissionStatus.refreshStatus(this)
        adapter.notifyDataSetChanged()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (PermissionUtils.isAllGranted(grantResults)) {
            permissionList[requestCode].isOpen = true
            adapter.notifyDataSetChanged()
        }
    }

    class Holder(view: View) : BaseListAdapter.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.title)
        val subtitle = view.findViewById<TextView>(R.id.subtitle)
        val open = view.findViewById<TextView>(R.id.open)
    }

    data class PermissionStatus(
            val permissionString: String,
            val permissionName: String,
            val desc: String,
            var isOpen: Boolean = false
    ) {
        companion object {
            private val permissions = listOf(
                    PermissionStatus("android.permission.BIND_ACCESSIBILITY_SERVICE", "无障碍", "操作界面"),
                    PermissionStatus("android.permission.SYSTEM_ALERT_WINDOW", "悬浮窗", "显示于其他界面之上"),
//                    PermissionStatus("android.permission.VIBRATE", "震动", ""),
                    PermissionStatus("android.permission.READ_CONTACTS", "联系人", "用于检索联系人"),
                    PermissionStatus("android.permission.CALL_PHONE", "电话", "用于拨打电话"),
                    PermissionStatus("android.permission.RECORD_AUDIO", "录音", "用于语音识别"),
                    PermissionStatus("android.permission.ACCESS_NETWORK_STATE", "获取网络状态", "用于获取网络状态"),
                    PermissionStatus("android.permission.INTERNET", "网络", ""),
                    PermissionStatus("android.permission.READ_PHONE_STATE", "读取设备状态", ""),
                    PermissionStatus("android.permission.WRITE_EXTERNAL_STORAGE", "写SD卡", ""),
                    PermissionStatus("android.permission.FLASHLIGHT", "闪光灯", "打开闪光灯"),
                    PermissionStatus("android.permission.CAMERA", "相机", "打开闪光灯")
            )

            fun refreshStatus(context: Context): List<PermissionStatus> {
                permissions.forEach {
                    it.isOpen = when {
                        it.permissionName == "悬浮窗" -> Build.VERSION.SDK_INT < Build.VERSION_CODES.M || PermissionUtils.canDrawOverlays(context)
                        it.permissionName == "无障碍" -> PermissionUtils.accessibilityServiceEnabled(context)
                        else -> PermissionUtils.isAllGranted(context, arrayOf(it.permissionString))
                    }
                }
                return permissions
            }
        }
    }

}
