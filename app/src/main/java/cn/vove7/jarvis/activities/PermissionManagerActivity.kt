package cn.vove7.jarvis.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.builder.buttons
import cn.vove7.bottomdialog.extension.awesomeHeader
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.AppPermission
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.log
import cn.vove7.common.bridges.InputMethodBridge
import cn.vove7.common.bridges.ShellHelper
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.utils.*
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.PermissionManagerActivity.PermissionStatus.Companion.allPerStr
import cn.vove7.jarvis.activities.base.OneFragmentActivity
import cn.vove7.jarvis.adapters.RecAdapterWithFooter
import cn.vove7.jarvis.databinding.FragmentBaseListBinding
import cn.vove7.jarvis.databinding.ListHeaderWithSwitchBinding
import cn.vove7.jarvis.fragments.SimpleListFragment
import cn.vove7.jarvis.jadb.JAdb
import cn.vove7.jarvis.receivers.AdminReceiver
import cn.vove7.jarvis.services.GestureService
import cn.vove7.jarvis.services.MyAccessibilityService
import cn.vove7.jarvis.view.dialog.contentbuilder.markdownContent
import cn.vove7.vtp.runtimepermission.PermissionUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.catchingnow.icebox.sdk_client.IceBox
import java.lang.Thread.sleep
import kotlin.concurrent.thread


/**
 * 权限管理
 */
class PermissionManagerActivity : OneFragmentActivity() {
    override var fragments: Array<androidx.fragment.app.Fragment> = arrayOf(ManageFragment.newIns())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //执行时消息
        if (intent.hasExtra("pName")) {
            GlobalApp.toastWarning(String.format(getString(R.string.text_operation_need_permission), intent.getStringExtra("pName")))
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
        var i = 0
        override var floatClickListener: View.OnClickListener? = View.OnClickListener {
            highLight(i++)
        }

        companion object {
            fun newIns(): ManageFragment = ManageFragment()
        }

        override fun initView(contentView: FragmentBaseListBinding) {
            adapter = buildAdapter()
            refreshStatus()
            recyclerView.isVerticalScrollBarEnabled = false
            buildHeader()

            val pn = activity?.intent?.getStringExtra("pName")?.let { pn ->
                permissions.indexOfFirst { it.permissionName == pn }
            }
            if (pn != null) {
                highLight(pn)
            }
        }

        private fun highLight(index: Int) {
            recyclerView.postDelayed(100) {
                (recyclerView.layoutManager as LinearLayoutManager)
                        .scrollToPositionWithOffset(index, 0)
                recyclerView.postDelayed(500) {
                    adapter.notifyItemChanged(index, "")
                }
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            refreshable = false
        }

        private fun buildHeader() {
            val vb = ListHeaderWithSwitchBinding.inflate(layoutInflater)
            val headerTitle = vb.headerTitle
            val headerSwitch = vb.headerSwitch
            headerTitle.text = "一键申请"
            headerSwitch.visibility = View.GONE
            vb.root.setOnClickListener { ActivityCompat.requestPermissions(requireActivity(), allPerStr, 100) }
            setHeader(vb.root)
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

                override fun onBindViewHolder(holder: RecViewHolder, position: Int, payloads: MutableList<Any>) {
                    if (payloads.isEmpty()) {
                        super.onBindViewHolder(holder, position, payloads)
                        return
                    }
                    val itemView = holder.itemView
                    val bgColor = ContextCompat.getColor(requireContext(), R.color.light_green_500) and 0x00ffffff
                    val bg = holder.itemView.background
                    ValueAnimator.ofInt(0x00, 0xef, 0x00).apply {
                        repeatCount = 2
                        duration = 500
                        addUpdateListener {
                            val a = it.animatedValue as Int
                            itemView.setBackgroundColor((a shl 24) or bgColor)
                        }
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                itemView.background = bg
                            }
                        })

                    }.start()
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
                        item.clickAction(item, requireActivity())
                    }
                }
            }
        }

        private fun openADB(ps: PermissionStatus, act: Activity) {
            BottomDialog.builder(requireActivity()) {
                awesomeHeader("无线ADB指南")

                markdownContent {
                    loadMarkdownFromUrl("https://gitee.com/v-assistant/static-files/raw/master/wireless_adb.md")
                }
                buttons {
                    if (!isWirelessAdbEnabled()) {
                        positiveButton("进入监听状态".spanColor(R.color.google_blue)) {
                            it.dismiss()
                            waitWirelessAdb()
                        }
                    } else {
                        positiveButton("测试连接".spanColor(R.color.google_green)) {
                            testAdbConnect()
                            it.dismiss()
                        }
                    }
                    neutralButton("复制此文档连接") {
                        SystemBridge.setClipText("https://gitee.com/v-assistant/static-files/blob/master/wireless_adb.md")
                        GlobalApp.toastSuccess("已复制")
                    }
                }
            }
        }

        @SuppressLint("CheckResult")
        private fun testAdbConnect() {
            MaterialDialog(requireActivity()).show {
                title(text = "测试ADB连接")
                noAutoDismiss()
                val gr = ResourcesCompat.getColor(resources, R.color.google_green, null)
                message(text = "请在稍后弹出的请求框中勾选[始终允许]，".span("[始终允许]", color = gr)
                        + "并点击[确定]按钮".span("[确定]", color = gr) + "\n点击下方[开始测试]进入授权"
                        .span("[开始测试]", color = gr)
                )
                cancelable(false)
                fun notifyResult(e: Throwable?) = runOnUi {
                    if (!this.isShowing) return@runOnUi
                    getActionButton(WhichButton.NEGATIVE).text = "重新测试"
                    getActionButton(WhichButton.NEGATIVE).show()
                    val c = if (e == null) R.color.google_green else R.color.google_red
                    message(text = (if (e == null) "测试通过" else "测试未通过：\n${e.message}\n*若点击[确认]后提示此消息，请重新测试。")
                            .spanColor(ResourcesCompat.getColor(resources, c, null)))
                    positiveButton(text = if (e == null) "完成" else "取消")
                }

                var t: Thread? = null
                fun startTest(dialog: MaterialDialog) {
                    message(text = "请在稍后弹出的请求框中勾选[始终允许]，".span("[始终允许]", color = gr)
                            + "并点击[确定]按钮".span("[确定]", color = gr))
                    dialog.getActionButton(WhichButton.NEGATIVE).gone()
                    t = thread {
                        val jadb = JAdb()
                        kotlin.runCatching {
                            if (jadb.connect(requireContext())) {
                                if (!AppPermission.canWriteSecureSettings) {
                                    AppPermission.autoOpenWriteSecureWithAdb(jadb)
                                }
                                notifyResult(null)
                            } else {
                                notifyResult(Exception("请确保已同意授权"))
                            }
                            jadb.close()
                        }.onFailure {
                            jadb.close()
                            notifyResult(it)
                        }
                    }
                }
                negativeButton(text = "开始测试", click = ::startTest)
                positiveButton(text = "取消") {
                    it.dismiss()
                    t?.interrupt()
                }
            }
        }

        private fun waitWirelessAdb() {
            MaterialDialog(requireActivity()).show {
                title(text = "等待开启ADB无线调试...")
                cancelable(false)
                message(text = "waiting...")
                val t = thread {
                    while (!isWirelessAdbEnabled()) {
                        try {
                            sleep(600)
                        } catch (e: InterruptedException) {
                            return@thread
                        }
                    }
                    runOnUi {
                        message(text = "开启成功！\n请断开数据线，点击下方[测试连接]")
                        positiveButton(text = "测试连接") {
                            it.dismiss()
                            testAdbConnect()
                            refreshStatus()
                        }
                    }
                }
                positiveButton(text = "取消") { t.interrupt() }
                onDismiss {
                    t.interrupt()
                }
            }
        }

        override fun onResume() {
            super.onResume()
            refreshStatus()
        }

        private fun isWirelessAdbEnabled() = SystemBridge.isWirelessAdbEnabled()

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
                    PermissionStatus(arrayOf("android.permission.BIND_DEVICE_ADMIN"), "设备管理器", getString(R.string.admin_desc)) r@{ it, act ->
                        if (it.isOpen) return@r
                        val mComponentName = ComponentName(act, AdminReceiver::class.java)
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName)
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            e.log()
                            GlobalApp.toastError("跳转失败，请手动进入[设置/安全/设备管理器]开启", 1)
                        }
                    },

                    PermissionStatus(arrayOf("ADB"), "无线ADB服务", getString(R.string.wireless_adb_desc), clickAction = ::openADB, isOpen = isWirelessAdbEnabled()),
                    PermissionStatus(arrayOf("ACCESSIBILITY_SERVICE"), "基础无障碍服务", getString(R.string.desc_accessibility), clickAction = openASAction),
                    PermissionStatus(arrayOf("ACCESSIBILITY_SERVICE2"), "高级无障碍服务（执行手势 Android7.0+）", getString(R.string.desc_gesc_accessibility), clickAction = openASAction),
                    PermissionStatus(arrayOf("android.permission.SYSTEM_ALERT_WINDOW"), "悬浮窗", "显示全局对话框、语音面板") { _, _ ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                PermissionUtils.requestDrawOverlays(requireActivity(), 0)
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
                        intent.data = Uri.parse("package:" + app.packageName)
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
                                ShellHelper.hasRoot()
                            }
                        }
                    },
                    PermissionStatus(arrayOf(), "WRITE_SECURE_SETTINGS", "自动切换输入法、开启无障碍服务\nroot和此权限有一即可") { it, _ ->
                        if (!it.isOpen) {
                            SystemBridge.openUrl("https://vove.gitee.io/2019/07/02/OOO/")
                        }
                    },
                    PermissionStatus(arrayOf("android.permission.READ_CONTACTS"), "联系人", "用于检索联系人，拨号指令"),
                    PermissionStatus(arrayOf(IceBox.SDK_PERMISSION), "冰箱", "用于启动和冻结冰箱管理的应用"),
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
                            it.isOpen = ShellHelper.hasRoot()
                            runOnUi { adapter.notifyDataSetChanged() }
                        }
                    }
                    it.permissionName == "无线ADB服务" -> isWirelessAdbEnabled()
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
