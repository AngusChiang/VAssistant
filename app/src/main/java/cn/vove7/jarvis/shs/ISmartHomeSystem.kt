package cn.vove7.jarvis.shs

import cn.vove7.common.app.AppConfig
import cn.vove7.common.bridges.SettingsBridge
import cn.vove7.jarvis.BuildConfig

/**
 * # ISmartHomeSystem
 * 家庭控制系统
 * @author Vove
 * 2019/8/18
 */
abstract class ISmartHomeSystem {
    abstract fun init()
    abstract fun isSupport(command: String): Boolean
    abstract fun doAction(command: String)
    val configs: MutableMap<String, String> = mutableMapOf()

    abstract fun test()

    //用于显示
    abstract fun summary(): String


    //保存配置到指令存储
    abstract fun saveInstConfig()

    internal fun parseConfig(s: String) {
        s.lines().forEach {
            if (it.trimStart().startsWith('#'))
                return@forEach

            val si = it.indexOf('=')
            if (si > 0) {
                val v = it.substring(si + 1).trim()
                if (v.isNotEmpty()) {
                    configs[it.substring(0, si).trim()] = v
                }
            }
        }
    }

    companion object {
        fun load(): ISmartHomeSystem? {
            return when (AppConfig.homeSystem) {
                0 -> RokidHomeSystem()
                else -> null
            }
        }

        fun templateConfig(type: Int): String {
            return if (type == 0) {
                //从指令存储读取
                val rokidSettings = SettingsBridge.getConfig("rokid")
                """
                    #webHook 地址
                    webHookUrl=${rokidSettings?.getString("rokid_url") ?: ""}
                    #设备序列号
                    deviceId=${rokidSettings?.getString("rokid_device_id") ?: ""}
                    #后台管理地址，用于获取房间列表和设备列表
                    adminUrl=${if (BuildConfig.DEBUG) "http://baduxiyang.oicp.net:9001" else ""}
                    #后台管理登录名
                    username=${if (BuildConfig.DEBUG) "admin" else ""}
                    #后台管理登录密码
                    pass=${if (BuildConfig.DEBUG) "admin" else ""}
                    #发送完成提示词
                    finishWord=若琪指令发送成功
                    """.trimIndent()
            } else ""
        }
    }
}