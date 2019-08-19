package cn.vove7.jarvis.shs

import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.HttpBridge
import cn.vove7.common.bridges.SettingsBridge
import cn.vove7.common.netacc.tool.base64
import cn.vove7.common.utils.orIn
import cn.vove7.common.utils.runInCatch
import cn.vove7.jarvis.services.MainService
import cn.vove7.paramregex.toParamRegex
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.net.NetHelper

/**
 * # RokidHomeSystem
 *
 * @author Vove
 * 2019/8/18
 */
class RokidHomeSystem : ISmartHomeSystem() {

    private val defaultDeviceList = setOf(
            "灯", "电灯", "筒灯", "大灯", "吊灯",
            "灯带", "布帘", "纱帘", "窗帘",
            "楼梯灯", "楼道灯", "门锁"
    )
    private val defaultRoomList = setOf(
            "客厅", "茶室", "餐厅",
            "厨房", "主卧", "卧室", "次卧",
            "衣帽间", "书房", "影视厅", "一楼",
            "二楼", "三楼", "车库", "门厅", "办公室",
            "地下室", "((#楼)?楼梯间)", "((#楼)?过道)"
    )

    private lateinit var deviceList: MutableSet<String>

    private lateinit var roomList: MutableSet<String>

    private val pointDeviceList = mutableSetOf<String>()


    /**
     * 初始化支持设备列表
     * 初始化用户信息
     */
    override fun init() {
        Vog.d("初始化Rokid")
        val config = AppConfig.homeSystemConfig

        if (config.isNullOrBlank()) {
            GlobalApp.toastInfo("请完善若琪配置")
            return
        }
        runInCatch(true) {
            parseConfig(config!!)
            roomList = defaultRoomList.toMutableSet()
            deviceList = defaultDeviceList.toMutableSet()
            pointDeviceList.clear()
            loadDataFromRemote()
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
                GlobalLog.log("loadDevices ${l.size}")
                l.forEach {
                    deviceList.add(it.name)
                    if (it.type == "com.fibaro.setPoint") {
                        pointDeviceList.add(it.name)
                    }
                }
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

    private val roomSensorsProperty = "(温度|湿度|亮度)"

    private lateinit var finishWord: String

    //设备操作
    //智能情景
    //房间温度、湿度、亮度
    override fun isSupport(command: String): Boolean {

        val roomReg = roomList.joinToString("|", "(", ")")
        val devReg = deviceList.joinToString("|", "(", ")")
        val pointsReg by lazy { pointDeviceList.joinToString("|", "(", ")") }

        val mm = "(帮我)?(打开|开启|关闭|关掉)$roomReg?的?$devReg%".toParamRegex().match(command)

        finishWord = configs["finishWord"] ?: "若琪指令发送成功"
        if (mm != null) {
            mm["g8"]?.also {
                finishWord = "好的，" + it + if (arrayOf("打开", "开启") orIn command) "已开启" else "已关闭"
            }
            return true
        }

        return ("$roomReg?的?$devReg%".toParamRegex().match(command) != null
                || "${roomReg}的?$roomSensorsProperty".toParamRegex().match(command) != null
                || (pointDeviceList.isNotEmpty() && "$roomReg?$pointsReg%".toParamRegex().match(command) != null)).also {
            Vog.d("若琪 $command isSupport: $it")
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
            MainService.instance?.speak(finishWord)
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
        )

        try {
            testExamples.forEach {
                Vog.d("test $it")
                if (isSupport(it.first) != it.second) {
                    throw Exception(it.toString())
                }
            }
            GlobalApp.toastSuccess("通过测试")
        } catch (e: Throwable) {
            e.printStackTrace()
            GlobalApp.toastError("未通过测试")
        }
    }

}

data class RokidDevice(val name: String, val type: String)
data class RokidRoom(val name: String)