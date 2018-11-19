package cn.vove7.jarvis.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import cn.vassistant.plugininterface.app.GlobalApp
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.PermissionManagerActivity.PermissionStatus.Companion.allPerStr
import cn.vove7.jarvis.activities.base.OneFragmentActivity
import cn.vove7.jarvis.adapters.RecAdapterWithFooter
import cn.vove7.jarvis.fragments.VListFragment
import cn.vove7.vtp.runtimepermission.PermissionUtils

/**
 * 权限管理
 */
class PermissionManagerActivity : OneFragmentActivity() {
    override var fragments: Array<Fragment> = arrayOf(ManageFragment.newIns(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //执行时消息
        if (intent.hasExtra("pName")) {
            GlobalApp.toastShort(String.format(getString(R.string.text_operation_need_permission)
                    , intent.getStringExtra("pName")))
        }
    }

    class ManageFragment : VListFragment() {
        lateinit var pActivity: Activity
        //        lateinit var permissionList: List<PermissionStatus>
        //        lateinit var adapter: BaseAdapter
        override fun clearDataSet() {
        }

        companion object {
            fun newIns(acti: AppCompatActivity): ManageFragment {
                val m = ManageFragment()
                m.pActivity = acti
                return m
            }
        }


        override fun initView(contentView: View) {
            adapter = buildAdapter()
            refreshStatus()
            recyclerView.isVerticalScrollBarEnabled = false
            buildHeader()
        }

        private fun buildHeader() {
            val v = layoutInflater.inflate(R.layout.list_header_with_switch, null, false)
            val headerTitle = v.findViewById<TextView>(R.id.header_title)
            val headerSwitch = v.findViewById<Switch>(R.id.header_switch)
            headerTitle.text = "一键申请"
            headerSwitch.visibility = View.GONE
            v.setOnClickListener { PermissionUtils.autoRequestPermission(pActivity, allPerStr) }
            setHeader(v)
        }

        override fun onGetData(pageIndex: Int) {
            refreshStatus()
            notifyLoadSuccess(true)
            adapter.hideFooterView()
        }

        private fun buildAdapter(): RecAdapterWithFooter<Holder> {
            return object : RecAdapterWithFooter<Holder>() {

                override fun itemCount(): Int = permissions.size

                override fun getItem(pos: Int): PermissionStatus? {
                    return permissions[pos]
                }

                override fun onCreateHolder(parent: ViewGroup, viewType: Int): Holder {
                    val view = layoutInflater.inflate(R.layout.item_of_permission_list, parent, false)
                    return Holder(view)
                }

                override fun onBindView(holder: Holder, position: Int, it: Any?) {
                    val item = it as PermissionStatus

                    holder.title.text = item.permissionName
                    if (item.desc == "") {
                        holder.subtitle.visibility = View.GONE
                    } else {
                        holder.subtitle.visibility = View.VISIBLE
                        holder.subtitle.text = item.desc
                    }
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
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        try {
                                            PermissionUtils.requestDrawOverlays(activity!!, 0)
                                        } catch (e: Exception) {
//                                            toast.showShort("跳转失败，请手动开启")
                                            try {
                                                SystemBridge.openAppDetail(context?.packageName
                                                    ?: "")
                                            } catch (e: Exception) {
                                                toast.showLong("跳转失败，请到应用详情手动开启")
                                            }
                                        }
                                    }
                                item.permissionName == "无障碍" -> {
                                    try {
                                        PermissionUtils.gotoAccessibilitySetting(activity!!)
                                    } catch (e: ActivityNotFoundException) {
                                        toast.showShort("跳转失败，请自行开启")
                                    }
                                }
                                else ->
                                    PermissionUtils.autoRequestPermission(activity!!,
                                            item.permissionString, position)
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

        override fun onRequestPermissionsResult(requestCode: Int, perm: Array<out String>, grantResults: IntArray) {
            if (PermissionUtils.isAllGranted(grantResults)) {
                permissions[requestCode].isOpen = true
                adapter.notifyDataSetChanged()
            }
        }

        val permissions by lazy{ listOf(
                PermissionStatus(arrayOf("android.permission.BIND_ACCESSIBILITY_SERVICE"), "无障碍", getString(R.string.desc_accessibility)),
                PermissionStatus(arrayOf("android.permission.SYSTEM_ALERT_WINDOW"), "悬浮窗", "显示全局对话框、语音面板"),
                PermissionStatus(arrayOf("android.permission.READ_CONTACTS"), "联系人", "用于检索联系人"),
                PermissionStatus(arrayOf("android.permission.CALL_PHONE"), "电话", "用于拨打电话"),
                PermissionStatus(arrayOf("android.permission.RECORD_AUDIO"), "录音", "用于语音识别"),
                PermissionStatus(arrayOf("android.permission.ACCESS_NETWORK_STATE"), "获取网络状态", "用于获取网络状态"),
                PermissionStatus(arrayOf("android.permission.INTERNET"), "网络", ""),
                PermissionStatus(arrayOf("android.permission.READ_PHONE_STATE"), "读取设备状态", ""),
                PermissionStatus(arrayOf("android.permission.WRITE_EXTERNAL_STORAGE"), "写SD卡", ""),
                PermissionStatus(arrayOf("android.permission.FLASHLIGHT"), "闪光灯", "打开闪光灯"),
                PermissionStatus(arrayOf("android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"), "位置信息", "不使用此类指令可不开启"),
//                        PermissionStatus(arrayOf("android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN"),
//                                "蓝牙", "打开蓝牙"),
                PermissionStatus(arrayOf("android.permission.CAMERA"), "相机", "打开闪光灯"),
                PermissionStatus(arrayOf("android.permission.READ_PHONE_STATE"), "读取设备状态", "个别机型需要"),
                PermissionStatus(arrayOf("android.permission.WRITE_CALENDAR",
                        "android.permission.READ_CALENDAR"), "日历", "读写日历")
        )}
        fun refreshStatus() {
            val context = GlobalApp.APP
            permissions.forEach {
                it.isOpen = when {
                    it.permissionName == "悬浮窗" -> Build.VERSION.SDK_INT < Build.VERSION_CODES.M || PermissionUtils.canDrawOverlays(context)
                    it.permissionName == "无障碍" -> PermissionUtils.accessibilityServiceEnabled(context)
                    else -> PermissionUtils.isAllGranted(context, it.permissionString)
                }
            }
            adapter.notifyDataSetChanged()
        }

    }

    class Holder(view: View) : RecAdapterWithFooter.RecViewHolder(view, null) {
        val title = view.findViewById<TextView>(R.id.title)
        val subtitle = view.findViewById<TextView>(R.id.subtitle)
        val open = view.findViewById<TextView>(R.id.open)
    }

    class PermissionStatus(
            val permissionString: Array<String>,
            val permissionName: String,
            val desc: String,
            var isOpen: Boolean = false
    ) {
        companion object {
            val allPerStr = arrayOf(
                    "android.permission.BIND_ACCESSIBILITY_SERVICE",
                    "android.permission.SYSTEM_ALERT_WINDOW",
                    "android.permission.READ_CONTACTS",
                    "android.permission.CALL_PHONE",
                    "android.permission.RECORD_AUDIO",
                    "android.permission.ACCESS_NETWORK_STATE",
                    "android.permission.INTERNET",
                    "android.permission.READ_PHONE_STATE",
                    "android.permission.WRITE_EXTERNAL_STORAGE",
                    "android.permission.FLASHLIGHT",
                    "android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.CAMERA"
            )

        }

    }
}
