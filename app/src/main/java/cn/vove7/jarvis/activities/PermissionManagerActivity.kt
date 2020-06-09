package cn.vove7.jarvis.activities

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import cn.vove7.admin_manager.AdminReceiver
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.AppPermission
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.app.log
import cn.vove7.common.bridges.InputMethodBridge
import cn.vove7.common.bridges.RootHelper
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.gotoAccessibilitySetting2
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.PermissionManagerActivity.PermissionStatus.Companion.allPerStr
import cn.vove7.jarvis.activities.base.OneFragmentActivity
import cn.vove7.jarvis.adapters.RecAdapterWithFooter
import cn.vove7.jarvis.fragments.SimpleListFragment
import cn.vove7.jarvis.services.GestureService
import cn.vove7.jarvis.services.MyAccessibilityService
import cn.vove7.vtp.runtimepermission.PermissionUtils
import kotlin.concurrent.thread


/**
 * 权限管理
 */
class PermissionManagerActivity : OneFragmentActivity() {
    override var fragments: Array<androidx.fragment.app.Fragment> = arrayOf(ManageFragment.newIns(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //执行时消息
        if (intent.hasExtra("pName")) {
            GlobalApp.toastWarning(String.format(getString(R.string.text_operation_need_permission)
                    , intent.getStringExtra("pName")))
        }
    }

    override fun onBackPressed() {
        if (intent.hasExtra("removeFromTask")) {
            finishAndRemoveTask()
        } else {
            super.onBackPressed()
        }
    }

    class ManageFragment : SimpleListFragment<PermissionStatus>() {
        lateinit var pActivity: Activity

        companion object {
            fun newIns(act: AppCompatActivity): ManageFragment {
                val m = ManageFragment()
                m.pActivity = act
                return m
            }
        }

        override fun initView(contentView: View) {
            adapter = buildAdapter()
            refreshStatus()
            recyclerView.isVerticalScrollBarEnabled = false
            buildHeader()
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            refreshable = false
        }

        private fun buildHeader() {
            val v = layoutInflater.inflate(R.layout.list_header_with_switch, null, false)
            val headerTitle = v.findViewById<TextView>(R.id.header_title)
            val headerSwitch = v.findViewById<Switch>(R.id.header_switch)
            headerTitle.text = "一键申请"
            headerSwitch.visibility = View.GONE
            v.setOnClickListener { ActivityCompat.requestPermissions(pActivity, allPerStr, 100) }
            setHeader(v)
        }

        override fun onLoadData(pageIndex: Int) {
            refreshStatus()
            changeViewOnLoadDone(true)
            adapter.hideFooterView()
        }

        private fun buildAdapter(): RecAdapterWithFooter<Holder, PermissionStatus> {
            return object : RecAdapterWithFooter<Holder, PermissionStatus>() {

                override fun itemCount(): Int = permissions.size

                override fun getItem(pos: Int): PermissionStatus? {
                    return permissions[pos]
                }

                override fun onCreateHolder(parent: ViewGroup, viewType: Int): Holder {
                    val view = layoutInflater.inflate(R.layout.item_of_permission_list, parent, false)
                    return Holder(view)
                }

                override fun onBindView(holder: Holder, position: Int, item: PermissionStatus) {
                    holder.title.text = item.permissionName
                    if (item.desc == "") {
                        holder.subtitle.visibility = View.GONE
                    } else {
                        holder.subtitle.visibility = View.VISIBLE
                        holder.subtitle.text = item.desc
                    }
                    holder.open.isChecked = item.isOpen
                    holder.title.isChecked = item.isOpen
                    if (item.isOpen) {
                        holder.open.text = getString(R.string.text_opened)
                    } else {
                        holder.open.text = getString(R.string.text_to_open)
                    }
                    holder.itemView.setOnClickListener {
                        item.clickAction(item, pActivity)
                    }
                }
            }
        }


        override fun onResume() {
            super.onResume()
            refreshStatus()
        }

        val permissions by lazy {
            val openASAction: (PermissionStatus, Activity) -> Unit = { it, act ->
                try {
                    PermissionUtils.gotoAccessibilitySetting2(act, if (it.permissionName == "基础无障碍服务")
                        MyAccessibilityService::class.java else GestureService::class.java)
                } catch (e: ActivityNotFoundException) {
                    GlobalApp.toastError("跳转失败，请自行开启")
                }
            }
            listOf(
                    PermissionStatus(arrayOf("android.permission.BIND_DEVICE_ADMIN"), "设备管理器", getString(cn.vove7.jarvis.R.string.admin_desc)) r@{ it, act ->
                        if (it.isOpen) return@r
                        val mComponentName = ComponentName(act, AdminReceiver::class.java)
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName)
                        try {
                            startActivityForResult(intent, 99)
                        } catch (e: Exception) {
                            e.log()
                            GlobalApp.toastError("跳转失败，请手动进入[设置/安全/设备管理器]开启", 1)
                        }
                    },
                    PermissionStatus(arrayOf("ACCESSIBILITY_SERVICE"), "基础无障碍服务", getString(R.string.desc_accessibility), clickAction = openASAction),
                    PermissionStatus(arrayOf("ACCESSIBILITY_SERVICE2"), "高级无障碍服务（执行手势 Android7.0+）", getString(R.string.desc_gesc_accessibility), clickAction = openASAction),
                    PermissionStatus(arrayOf("android.permission.SYSTEM_ALERT_WINDOW"), "悬浮窗", "显示全局对话框、语音面板") { _, _ ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                PermissionUtils.requestDrawOverlays(activity!!, 0)
                            } catch (e: Exception) {
//                                            toast.showShort("跳转失败，请手动开启")
                                try {
                                    SystemBridge.openAppDetail(context?.packageName
                                            ?: "")
                                } catch (e: Exception) {
                                    GlobalApp.toastError("跳转失败，请到应用详情手动开启")
                                }
                            }
                        }
                    },
                    PermissionStatus(arrayOf("android.permission.WRITE_SETTINGS"), "修改系统设置", "用于调节屏幕亮度") r@{ it, app ->
                        if (it.isOpen || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return@r
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                        intent.setData(Uri.parse("package:" + app.packageName))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            app.startActivity(intent)
                        } catch (e: Exception) {
                            SystemBridge.openAppDetail(app.packageName)
                        }
                    },
                    PermissionStatus(arrayOf(), "输入法", """用于更强大的编辑操作
                            |提示；在执行编辑框操作时，会自动切换内置输入法进行操作，结束后会恢复原输入法。
                            |自动切换输入法支持三种方式：
                            |1. 无障碍服务（可见的切换步骤）
                            |2. Root权限（推荐）
                            |3. WRITE_SECURE_SETTINGS权限（推荐，开启方法，见[常见问题]）
                            |由于每次询问Root权限申请过慢，请预先授权。""".trimMargin()
                    ) { it, app ->
                        if (!it.isOpen) {
                            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                        }
                    },
                    PermissionStatus(arrayOf(), "Root", "自动切换输入法、开启无障碍服务\n授予Root权限时，请将打开Magisk Manager后台运行（若使用Msgisk管理授权）") { it, _ ->
                        if (!it.isOpen) {
                            launch {
                                //防止阻塞主线程
                                RootHelper.hasRoot()
                            }
                        }
                    },
                    PermissionStatus(arrayOf(), "WRITE_SECURE_SETTINGS", "自动切换输入法、开启无障碍服务\nroot和此权限有一即可") { it, _ ->
                        if (!it.isOpen) {
                            SystemBridge.openUrl("https://vove.gitee.io/2019/07/02/OOO/")
                        }
                    },
                    PermissionStatus(arrayOf("android.permission.READ_CONTACTS"), "联系人", "用于检索联系人，拨号指令"),
                    PermissionStatus(arrayOf("android.permission.CALL_PHONE"), "电话", "用于拨打电话"),
                    PermissionStatus(arrayOf("android.permission.RECORD_AUDIO"), "录音", "用于语音识别"),
                    PermissionStatus(arrayOf("android.permission.ACCESS_NETWORK_STATE"), "获取网络状态", "用于获取网络状态"),
                    PermissionStatus(arrayOf("android.permission.INTERNET"), "网络", ""),
                    PermissionStatus(arrayOf("android.permission.READ_PHONE_STATE"), "读取设备状态", ""),
                    PermissionStatus(arrayOf("android.permission.WRITE_EXTERNAL_STORAGE"), "写SD卡", ""),
                    PermissionStatus(arrayOf("android.permission.FLASHLIGHT"), "闪光灯", "打开闪光灯"),
                    PermissionStatus(arrayOf(
                            "android.permission.ACCESS_COARSE_LOCATION",
                            "android.permission.ACCESS_FINE_LOCATION"
                    ), "位置信息", "不使用此类指令可不开启"),
//                        PermissionStatus(arrayOf("android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN"),
//                                "蓝牙", "打开蓝牙"),
                    PermissionStatus(arrayOf("android.permission.CAMERA"), "相机", "打开闪光灯"),
                    PermissionStatus(arrayOf("android.permission.READ_PHONE_STATE"), "读取设备状态", "个别机型需要"),
                    PermissionStatus(arrayOf("android.permission.WRITE_CALENDAR",
                            "android.permission.READ_CALENDAR"), "日历", "读写日历")
            )
        }

        /**
         * 刷新权限状态
         */
        private fun refreshStatus() = launch {
            val context = GlobalApp.APP
            permissions.forEach {
                it.isOpen = when {
                    it.permissionName == "Root" -> false.also { _ ->
                        //异步获取root权限
                        thread {
                            it.isOpen = RootHelper.hasRoot()
                            runOnUi { adapter.notifyDataSetChanged() }
                        }
                    }
                    it.permissionName == "输入法" -> InputMethodBridge.isEnable
                    it.permissionName == "WRITE_SECURE_SETTINGS" -> AppPermission.canWriteSecureSettings
                    it.permissionName == "修改系统设置" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Settings.System.canWrite(context)
                    } else true
                    it.permissionName == "设备管理器" -> AdminReceiver.isActive()
                    it.permissionName == "悬浮窗" -> Build.VERSION.SDK_INT < Build.VERSION_CODES.M || PermissionUtils.canDrawOverlays(context)
                    it.permissionString[0] == "ACCESSIBILITY_SERVICE" ->
                        AccessibilityApi.isBaseServiceOn
                    it.permissionString[0] == "ACCESSIBILITY_SERVICE2" ->
                        AccessibilityApi.isAdvanServiceOn
                    else -> PermissionUtils.isAllGranted(context, it.permissionString)
                }
            }
            runOnUi { adapter.notifyDataSetChanged() }
        }
    }

    class Holder(view: View) : RecAdapterWithFooter.RecViewHolder(view, null) {
        val title = view.findViewById<CheckedTextView>(R.id.title)
        val subtitle = view.findViewById<CheckedTextView>(R.id.subtitle)
        val open = view.findViewById<CheckedTextView>(R.id.open)
    }

    class PermissionStatus(
            val permissionString: Array<String>,
            val permissionName: String,
            val desc: String,
            var isOpen: Boolean = false,
            val clickAction: (PermissionStatus, Activity) -> Unit = a@{ it, act ->
                if (it.isOpen) return@a
                ActivityCompat.requestPermissions(act, it.permissionString, 100)
            }
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
                    "android.permission.CAMERA",
                    "android.permission.WRITE_CALENDAR",
                    "android.permission.READ_CALENDAR"
            )

        }

    }
}
