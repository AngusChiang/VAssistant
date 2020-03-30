package cn.vove7.jarvis.shs

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.widget.PopupMenu
import cn.vove7.bottomdialog.BottomDialog
import cn.vove7.bottomdialog.extension.awesomeHeader
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.HttpBridge
import cn.vove7.common.bridges.SettingsBridge
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.net.tool.base64
import cn.vove7.common.utils.anyIn
import cn.vove7.common.utils.content
import cn.vove7.common.utils.runInCatch
import cn.vove7.jarvis.R
import cn.vove7.jarvis.plugins.PluginConfig
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.view.CheckBoxItem
import cn.vove7.jarvis.view.IntentItem
import cn.vove7.jarvis.view.SettingChildItem
import cn.vove7.jarvis.view.dialog.TextEditorDialog
import cn.vove7.jarvis.view.dialog.contentbuilder.markdownContent
import cn.vove7.jarvis.view.dialog.editorView
import cn.vove7.jarvis.work.RokidSendLocTask
import cn.vove7.paramregex.toParamRegex
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.net.NetHelper
import cn.vove7.vtp.runtimepermission.PermissionUtils
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * # RokidHomeSystem
 *
 * @author Vove
 * 2019/8/18
 */
class RokidHomeSystem : ISmartHomeSystem() {
    override val name: String = "若琪"

    private val defaultDeviceList = setOf(
            "灯", "电灯", "筒灯", "大灯", "吊灯", "产品灯",
            "灯带", "布帘", "纱帘", "窗帘",
            "楼梯灯", "楼道灯", "门锁", "门", "踢脚灯",
            "水阀", "机械手", "阀门"

    )
    private val defaultRoomList = setOf(
            "客厅", "茶室", "餐厅",
            "厨房", "主卧", "卧室", "次卧", "仓库",
            "衣帽间", "书房", "影视厅", "一楼",
            "二楼", "三楼", "车库", "门厅", "办公室",
            "地下室", "((#楼)?楼梯间)", "((#楼)?过道)", "地下室"
    )

    //防止解析语音指令执行时，未初始化
    private var deviceList: MutableSet<String> = defaultRoomList.toMutableSet()
    private var roomList: MutableSet<String> = defaultDeviceList.toMutableSet()
    private val getRoomReg get() = roomList.joinToString("|", "(", ")")
    private val getDevReg get() = deviceList.joinToString("|", "(", ")")
    private val getPDevReg get() = pointDeviceList.joinToString("|", "(", ")")

    private val pointDeviceList = mutableSetOf<String>()

    /**
     * 初始化支持设备列表
     * 初始化用户信息
     */
    override fun init() {
        super.init()

        Vog.d("初始化Rokid")
        val config = AppConfig.homeSystemConfig

        if (config.isNullOrBlank()) {
            GlobalApp.toastInfo("请完善若琪配置")
            return
        }
        runInCatch(true) {
            roomList = defaultRoomList.toMutableSet()
            deviceList = defaultDeviceList.toMutableSet()
            parseConfig(config)
            pointDeviceList.clear()
            loadDataFromRemote()
            if (PluginConfig.rokidInTimeSendLocation) {
                startSendLocTask(6000)
            }
        }
    }

    private fun loadDataFromRemote() {
        val url = configs["adminUrl"]
        if (url == null || url.isBlank()) {
            GlobalLog.log("若琪地址配置错误：$url")
            return
        } else {
            loadRooms(url)
            loadDevices(url)
        }
    }

    private fun loadDevices(url: String) {
        //设备列表
        NetHelper.get<List<RokidDevice>>("$url/api/devices", mapOf(
                "Authorization" to "Basic " + "${configs["username"]}:${configs["pass"]}".base64
        )) {
            fail { _, exception ->
                GlobalLog.err("加载设备列表失败：${exception.message}")
                GlobalApp.toastError("若琪用户认证失败，请检查用户名及密码")
            }
            success { _, s ->
                val numReg = "[0-9.()（）]+".toRegex()
                val l = s.filter { !numReg.matches(it.name) }
                l.filter { it.name.length > 1 && "^[\u4e00-\u9fa5]+$".toRegex().matches(it.name) }.forEach {
                    deviceList.add(it.name)
                    if (it.type == "com.fibaro.setPoint") {
                        pointDeviceList.add(it.name)
                    }
                }
                GlobalLog.log("loadDevices ${deviceList.size}")
            }
        }
    }

    private fun loadRooms(url: String) {
        //设备列表
        NetHelper.get<List<RokidRoom>>("$url/api/rooms", mapOf(
                "Authorization" to "Basic " + "${configs["username"]}:${configs["pass"]}".base64
        )) {
            fail { _, exception ->
                GlobalLog.err("加载房间列表失败：${exception.message}")
            }
            success { _, s ->
                val numReg = "[0-9.()（）]+".toRegex()
                val l = s.filter { !numReg.matches(it.name) }

                GlobalLog.log("loadRooms ${l.size}")
                l.forEach {
                    roomList.add(it.name)
                }
            }
        }
    }

    private val roomSensorsProperty = "(温度|湿度|亮度|音量|暂停|开始|播放|(换|上|下)一首)"

    private lateinit var finishWord: String

    //设备操作
    //智能情景
    //房间温度、湿度、亮度
    override fun isSupport(command: String): Boolean {

        val pointsReg by lazy { getPDevReg }
        val devReg = getDevReg
        val roomReg = getRoomReg

        val mm = "(帮我)?(打开|开启|启动|关闭|关掉|开|关)$roomReg?的?$devReg%".toParamRegex().match(command)

        finishWord = configs["finishWord"] ?: "若琪指令发送成功"
        if (mm != null) {
            mm["g8"]?.also {
                finishWord = "好的，" + it + if (arrayOf("打开", "启动", "开启") anyIn command) "已开启" else "已关闭"
            }
            return true
        }

        //书房播放音乐
        return (
                //房间设备操作
                "$roomReg?的?$devReg%".toParamRegex().match(command) != null
                        //房间 感应器 操作
                        || "${roomReg}的?$roomSensorsProperty%".toParamRegex().match(command) != null
                        || (pointDeviceList.isNotEmpty() && "$roomReg?$pointsReg%".toParamRegex().match(command) != null)
                        //自定义
                        || inUserCommand(command, roomReg, devReg)
                        //智能场景
                        || smartEnv(command)
                ).also {
            Vog.d("若琪 $command isSupport: $it")
        }
    }

    private fun smartEnv(command: String): Boolean {
        return arrayOf("我想去看电影", "我想听%", "(打开|开启|启动|关闭)(自动|浪漫|派对|就餐|会客)模式").any {
            it.toParamRegex().match(command) != null
        }
    }

    override fun inUserCommand(command: String, vararg extras: Any): Boolean {
        return userCommands.any {
            it.replace("{room}", extras[0] as String).replace("{device}", extras[1] as String).toParamRegex().match(command) != null
        }
    }

    override fun saveInstConfig() {
        val rokidSettings = SettingsBridge.getConfig("rokid") ?: return
        configs["webHookUrl"]?.also {
            if (!it.isBlank()) {
                rokidSettings.set("rokid_url", it)
            }
        }
        configs["deviceId"]?.also {
            if (!it.isBlank()) {
                rokidSettings.set("rokid_device_id", it)
            }
        }
    }

    override fun doAction(command: String) {
        val hookUrl = configs["webHookUrl"]
        if (hookUrl == null) {
            GlobalApp.toastWarning("请填写若琪: webHookUrl")
            return
        }
        val devId = configs["deviceId"]
        if (devId == null) {
            GlobalApp.toastWarning("请填写若琪配置: deviceId")
            return
        }
        val resp = HttpBridge.postJson(hookUrl, mapOf(
                "type" to "asr",
                "devices" to mapOf("sn" to devId),
                "data" to mapOf("text" to command)
        ))

        if (resp?.startsWith("Congratulations") == true) {
            MainService.speak(finishWord)
        } else {
            GlobalApp.toastError("若琪指令发送失败：$resp")
            GlobalLog.err("若琪指令发送失败：$resp")
        }
    }

    override fun summary(): String = buildString {
        appendln("支持的指令说法：(打开/关闭/关掉){房间名}{设备名}、打开{设备名}")
        appendln("### 房间列表")
        appendln(roomList.joinToString(", "))
        appendln("### 设备列表")

        appendln(deviceList.joinToString(", "))
    }


    override fun test() {
        val testExamples = listOf(
                "打开客厅大灯" to true,
                "打开5楼过道大灯" to true,
                "帮我打开5楼过道大灯" to true,
                "关掉5楼过道大灯" to true,
                "关掉过道大灯" to true,
                "关掉客厅大灯" to true,
                "关掉客厅的大灯" to true
                , "打开客厅的电灯" to true
                , "关掉客厅大灯" to true
                , "客厅大灯" to true
                , "客厅空调温度低一点" to true
                , "客厅大灯亮一点" to true
                , "办公室空调自动模式" to true
                , "打开QQ" to false
                , "办公室温度" to true
                , "打开客厅电视" to true
                , "关闭客厅电视" to true
                , "打开自动模式" to true
                , "关闭自动模式" to true
                , "办公室空调制冷模式" to true
                , "办公室空调调到25度" to true
                , "办公室空调自动模式" to true
                , "打开办公室空调" to true
                , "关闭办公室空调" to true
                , "关闭推窗器" to true
                , "打开推窗器" to true
                , "关闭车库门" to true
                , "打开车库门" to true
                , "客厅空调制冷模式" to true
                , "关闭客厅空调" to true
                , "打开客厅空调" to true
                , "打开客厅门布帘" to true
                , "关闭客厅门布帘" to true
                , "打开客厅门纱帘" to true
                , "关闭客厅门纱帘" to true
                , "播放音乐" to true
                , "暂停播放" to true
                , "继续播放" to true
                , "换一首" to true
                , "上一首" to true
                , "下一首" to true
                , "我想听小相思" to true
                , "餐厅空调制冷模式" to true
                , "打开浪漫模式" to true
                , "关闭浪漫模式" to true
                , "打开派对模式" to true
                , "关闭派对模式" to true
                , "打开就餐模式" to true
                , "关闭就餐模式" to true
                , "打开会客模式" to true
                , "关闭会客模式" to true
                , "我想去看电影" to true
                , "打开客厅门窗帘" to true
                , "关闭客厅门窗帘" to true
                , "客厅灯带换个颜色" to true
                , "关闭水阀" to true
                , "打开水阀" to true
                , "音量30%" to true
                , "音量10%" to true
                , "书房播放音乐" to true
                , "书房暂停播放" to true
                , "客厅音量调高五十" to true
                , "客厅音量调到10" to true
                , "客厅音量调到20" to true
                , "客厅音量调到30" to true
                , "客厅音量调到十" to true
                , "客厅音量调到二十" to true
                , "客厅音量调到三十" to true
                , "餐厅暂停播放" to true
                , "客厅继续播放" to true
                , "餐厅播放音乐" to true
                , "餐厅播放音乐" to true
                , "餐厅播放上一首" to true
                , "餐厅播放下一首" to true
                , "客厅播放音乐" to true
                , "客厅暂停播放" to true
                , "客厅音量调到10" to true
                , "客厅音量调到20" to true
                , "客厅换一首" to true
                , "打开地下室产品灯" to true
                , "关闭地下室产品灯" to true
                , "打开地下室过道灯" to true
                , "关闭地下室过道灯" to true
                , "餐厅换一首" to true
                , "客厅大灯亮度100" to true
        )
        //未通过 提那家自定义短语
        var suc = true
        testExamples.forEach {
            if (isSupport(it.first) != it.second) {
                Vog.e("失败：$it")
                suc = false
            } else {
                Vog.d("通过 $it")
            }
        }
        if (suc) {
            GlobalApp.toastSuccess("测试通过")
        } else {
            GlobalApp.toastError("未通过测试")
        }
    }

    override fun getSettingItems(context: Context): Array<SettingChildItem> = arrayOf(
            IntentItem(title = "参数配置") {
                val s = AppConfig.homeSystem
                if (s == null) {
                    GlobalApp.toastInfo("请先选择您的家居系统")
                    return@IntentItem
                }
                TextEditorDialog(context, AppConfig.homeSystemConfig
                    ?: templateConfig(s)) {
                    noAutoDismiss()
                    title(text = "参数配置")
                    editorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                    positiveButton(text = "保存") {
                        val text = editorView.content()
                        AppConfig.homeSystemConfig = text
                        //重新解析配置，并保存到对应的指令存储中
                        MainService.homeControlSystem?.apply {
                            init()
                            saveInstConfig()
                        }
                        GlobalApp.toastSuccess("保存完成")
                        it.dismiss()
                    }
                    neutralButton(text = "选择模板") {
                        PopupMenu(context, it.getActionButton(WhichButton.NEUTRAL)).apply {
                            menu.add(0, 0, 0, "Rokid(若琪)")
                            setOnMenuItemClickListener { i ->
                                editorView.setText(templateConfig(i.itemId))
                                true
                            }
                            show()
                        }
                    }
                    negativeButton { it.dismiss() }
                }
            },
            IntentItem(title = "查看信息") {
                if (AppConfig.homeSystem == null) {
                    GlobalApp.toastInfo("请先选择您的家居系统")
                    return@IntentItem
                }
                BottomDialog.builder(context as Activity) {
                    awesomeHeader("信息")
                    markdownContent {
                        loadMarkdown(MainService.homeControlSystem?.summary()
                            ?: "")
                    }
                }
            },
            CheckBoxItem(
                    title = "实时位置服务",
                    keyId = R.string.key_rokid_send_loc,
                    summary = "远程地址请在参数配置中设置[realTimeLocUrl]"
            ) { _, b ->
                if (b) {
                    if (!PermissionUtils.isAllGranted(context, arrayOf("android.permission.ACCESS_COARSE_LOCATION",
                                    "android.permission.ACCESS_FINE_LOCATION"))) {
                        GlobalApp.toastInfo("请先开启定位权限")
                        return@CheckBoxItem false
                    }
                    startSendLocTask()
                } else {
                    stopSendLocTask()
                }
                true
            },
            IntentItem(title = "设置家庭地址", summary = PluginConfig.rokidHomeLoc?.let { "${it.first}, ${it.second}" }
                ?: "设置当前位置为家庭地址") {
                GlobalScope.launch {
                    GlobalApp.toastInfo("正在获取位置...")
                    val loc = SystemBridge.location()
                    if (loc == null) {
                        GlobalApp.toastInfo("位置获取失败")
                        return@launch
                    } else {
                        PluginConfig.rokidHomeLoc = loc.longitude to loc.latitude
                    }
                    withContext(Dispatchers.Main) {
                        kotlin.runCatching {
                            it.summary = PluginConfig.rokidHomeLoc?.let { "${it.first}, ${it.second}" }
                        }
                    }
                    GlobalApp.toastSuccess("设置完成")
                }

            }
    )


    private val sendLocTask by lazy {
        RokidSendLocTask { configs }
    }

    private fun startSendLocTask(delay: Long = 3000) {
        stopSendLocTask()
        GlobalLog.log("若琪：启动位置发送服务")
        GlobalApp.toastInfo("若琪：启动位置发送服务")
        sendLocTask.nextTask(delay)
    }

    private fun stopSendLocTask() = sendLocTask.stop()

    fun callSendLocTask() {
        if(PluginConfig.rokidInTimeSendLocation) {
            sendLocTask.run()
        }
    }

}

data class RokidDevice(val name: String, val type: String)
data class RokidRoom(val name: String)