package cn.vove7.jarvis.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import cn.vove7.android.common.ext.delayRun
import cn.vove7.android.common.loge
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
import cn.vove7.jadb.AdbClient
import cn.vove7.jadb.AdbInvalidPairingCodeException
import cn.vove7.jadb.AdbMdns
import cn.vove7.jadb.AdbPairingClient
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.PermissionManagerActivity.PermissionStatus.Companion.allPerStr
import cn.vove7.jarvis.activities.base.OneFragmentActivity
import cn.vove7.jarvis.adapters.RecAdapterWithFooter
import cn.vove7.jarvis.databinding.FragmentBaseListBinding
import cn.vove7.jarvis.databinding.ListHeaderWithSwitchBinding
import cn.vove7.jarvis.fragments.SimpleListFragment
import cn.vove7.jarvis.receivers.AdminReceiver
import cn.vove7.jarvis.services.GestureService
import cn.vove7.jarvis.services.MyAccessibilityService
import cn.vove7.jarvis.view.dialog.contentbuilder.markdownContent
import cn.vove7.vtp.extend.disable
import cn.vove7.vtp.extend.enable
import cn.vove7.vtp.runtimepermission.PermissionUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.input.getInputLayout
import com.afollestad.materialdialogs.input.input
import com.catchingnow.icebox.sdk_client.IceBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep
import java.net.Inet4Address
import kotlin.concurrent.thread


/**
 * 权限管理
 */
class PermissionManagerActivity : OneFragmentActivity() {
    override var fragments: Array<Fragment> = arrayOf(ManageFragment.newIns())


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
            var WAIT_WIRE_ADB = false
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
            if (WAIT_WIRE_ADB && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                waitAdbPair()
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
                        positiveButton("进入监听状态".spanColor(getColor(requireContext(), R.color.google_blue))) {
                            it.dismiss()
                            waitWirelessAdb()
                        }
                    } else {
                        positiveButton("测试连接".spanColor(getColor(requireContext(), R.color.google_green))) {
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
                        val jadb = AdbClient(requireContext())
                        kotlin.runCatching {
                            jadb.connect()
                            if (!AppPermission.canWriteSecureSettings) {
                                AppPermission.autoOpenWriteSecureWithAdb(jadb)
                            }
                            notifyResult(null)
                            jadb.close()
                        }.onFailure {
                            notifyResult(Exception("请确保已同意授权"))
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

        private fun toDevSettings() {
            startActivity(Intent("android.settings.APPLICATION_DEVELOPMENT_SETTINGS").also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    it.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                }
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }

        @SuppressLint("CheckResult")
        @RequiresApi(Build.VERSION_CODES.R)
        private fun waitAdbPair() = MaterialDialog(requireContext()).show {
            title(text = "无线ADB配对")
            val mwm = requireActivity().isInMultiWindowMode
            cancelable(false)
            noAutoDismiss()
            message(text = if (mwm) "正在搜索配对端口..." else "1. 请先进入分屏状态\n" +
                "2. 进入[开发者选项/无线调试]\n3. 开启无线调试选项\n4. 点击 [使用配对码配对设备]")
            if (!mwm) {
                WAIT_WIRE_ADB = true
            } else {
                WAIT_WIRE_ADB = false
                toDevSettings()
            }
            if (mwm) {
                positiveButton(text = "跳转开发者选项") {
                    runOnUiDelay(100, ::toDevSettings)
                }
            }

            negativeButton(text = "取消") {
                it.dismiss()
            }

            if (mwm) {
                val pairPort = MutableLiveData(0)
                val mdns = AdbMdns(requireContext(), AdbMdns.TLS_PAIRING, pairPort)

                pairPort.observe(requireActivity()) { port ->
                    if (port in 1024..65535) {
                        message(text = "发现端口 $port, 请输入配对码进行连接")
                        try {
                            getInputLayout().show()
                        } catch (e: Throwable) {
                            input(hint = "配对码", inputType = InputType.TYPE_CLASS_NUMBER) { d, s ->
                                doAdbPair(pairPort.value?:0, s.toString().trim(), this, mdns)
                                getInputLayout().disable()
                                getActionButton(WhichButton.POSITIVE).disable()
                            }
                        }
                        positiveButton(text = "连接")
                    } else {
                        kotlin.runCatching {
                            message(text = "正在搜索配对端口...")
                            getInputLayout().gone()
                        }
                    }
                }
                onDismiss { mdns.stop() }
                mdns.start()
            }
        }

        @RequiresApi(Build.VERSION_CODES.R)
        private fun doAdbPair(port: Int, code: String, dialog: MaterialDialog, mdns: AdbMdns) = thread {
            val pairClient = AdbPairingClient(requireContext(),
                Inet4Address.getLoopbackAddress().hostName,
                port, code
            )
            kotlin.runCatching {
                if (pairClient.start()) {// 配对成功，连接 tcpip5555
                    mdns.stop()
                    runOnUi { enableAdbPort5555(dialog) }
                } else {
                    error("配对失败，未知错误")
                }
            }.onFailure {
                val error = if (it is AdbInvalidPairingCodeException) {
                    "配对失败，请确认配对码正确"
                } else it.message ?: ""
                runOnUi {
                    dialog.message(text = error.spanColor(getColor(requireContext(), R.color.google_red)))
                    dialog.getInputLayout().enable()
                    dialog.getActionButton(WhichButton.POSITIVE).enable()
                }
                //失败
            }
        }

        private fun enableAdbPort5555(dialog: MaterialDialog) {
            dialog.getInputLayout().gone()
            dialog.title(text = "请稍等")
            dialog.message(text = "find connect port...")
            val conPort = MutableLiveData(0)

            suspend fun showResult(content: String, btnText: String) = withContext(Dispatchers.Main) {
                dialog.title(text = "无线配对")
                dialog.message(text = content)
                dialog.getActionButton(WhichButton.POSITIVE).apply {
                    text = btnText
                    setOnClickListener {
                        dialog.dismiss()
                    }
                }.enable()
            }

            val mdns = AdbMdns(requireContext(), AdbMdns.TLS_CONNECT, conPort)
            conPort.observe(requireActivity()) { port ->
                if (port in 1024..65535) {
                    mdns.stop()
                    dialog.message(text = "tcpip 5555 on $port...")
                    lifecycleScope.launch(Dispatchers.IO) {
                        kotlin.runCatching {
                            val cli = AdbClient(requireContext(), port = port)
                            cli.connect()
                            if (!AppPermission.canWriteSecureSettings) {
                                AppPermission.autoOpenWriteSecureWithAdb(cli)
                            }
                            cli.tcpip(5555)
                            delay(500)
                        }.onSuccess {
                            showResult("恭喜！开启完成\nport: ${SystemBridge.adbPort()}", "完成")
                            refreshStatus()
                        }.onFailure {
                            it.loge()
                            showResult("OH! 开启失败，请重试\n${it}", "退出")
                        }
                    }
                }
            }
            dialog.onDismiss {
                mdns.stop()
            }
            mdns.start()
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    neutralButton(text = "无线配对") {
                        waitAdbPair()
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

        private fun openASAction(it: PermissionStatus, act: Activity) {
            val base = it.permissionName == "基础无障碍服务"
            if (!it.isOpen) {
                launch {
                    val whichService = if (base) AccessibilityApi.WHICH_SERVICE_BASE
                    else AccessibilityApi.WHICH_SERVICE_GESTURE
                    if (AccessibilityApi.autoOpenService(whichService,
                            checkAfter = true, failByUser = true, toast = false)) {
                        GlobalApp.toastInfo("自动开启成功")
                        refreshStatus()
                    } else {
                        GlobalApp.toastInfo("自动开启失败，请手动开启")
                    }

                }
            } else {
                PermissionUtils.gotoAccessibilitySetting2(act, if (base)
                    MyAccessibilityService::class.java else GestureService::class.java)
            }
        }

        val permissions: List<PermissionStatus> by lazy {
            mutableListOf(
                PermissionStatus(arrayOf("android.permission.BIND_DEVICE_ADMIN"), "设备管理器", getString(R.string.admin_desc)) r@{ it, act ->
                    if (it.isOpen) {
                        val s = SystemBridge.startActivity("com.android.settings", "com.android.settings.DeviceAdminSettings") ||
                            SystemBridge.startActivity("com.android.settings", "com.android.settings.Settings\$DeviceAdminSettingsActivity")
                        if (!s) {
                            GlobalApp.toastWarning("跳转设备管理设置失败，请手动在设置中搜索改设置")
                            delayRun(2000) {
                                startActivity(Intent(Settings.ACTION_SETTINGS))
                            }
                        }
                        return@r
                    }
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
                PermissionStatus(
                    arrayOf("ADB"),
                    "无线ADB服务", getString(R.string.wireless_adb_desc),
                    clickAction = ::openADB,
                    isOpen = isWirelessAdbEnabled()
                ),
                PermissionStatus(
                    arrayOf("ACCESSIBILITY_SERVICE"),
                    "基础无障碍服务",
                    getString(R.string.desc_accessibility),
                    clickAction = ::openASAction),
                PermissionStatus(arrayOf("ACCESSIBILITY_SERVICE2"), "高级无障碍服务（执行手势 Android7.0+）", getString(R.string.desc_gesc_accessibility), clickAction = ::openASAction),
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
                ) { it, _ ->
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
                    "android.permission.READ_CALENDAR"), "日历", "管理日历事件")
            ).apply {
                if (SystemBridge.hasInstall(IceBox.PACKAGE_NAME)) {
                    this += (PermissionStatus(arrayOf(IceBox.SDK_PERMISSION), "冰箱", "用于启动和冻结冰箱管理的应用"))
                }
            }
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
                    it.permissionName == "设备管理器" -> {
                        AdminReceiver.isActive().also { acted ->
                            it.desc = getString(R.string.admin_desc) +
                                (if (acted) "\n点击跳转设备管理设置" else "")
                        }
                    }
                    it.permissionName == "悬浮窗" -> Build.VERSION.SDK_INT < Build.VERSION_CODES.M || PermissionUtils.canDrawOverlays(context)
                    it.permissionString[0] == "ACCESSIBILITY_SERVICE" ->
                        AccessibilityApi.isBaseServiceOn
                    it.permissionString[0] == "ACCESSIBILITY_SERVICE2" ->
                        AccessibilityApi.isGestureServiceOn
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
        var desc: String,
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
