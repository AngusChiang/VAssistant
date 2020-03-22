package cn.vove7.jarvis.shs

import android.content.Context
import androidx.annotation.CallSuper
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.SettingsBridge
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.view.SettingChildItem
import cn.vove7.paramregex.toParamRegex
import cn.vove7.vtp.net.GsonHelper

/**
 * # ISmartHomeSystem
 * 家庭控制系统
 * @author Vove
 * 2019/8/18
 */
abstract class ISmartHomeSystem {
    abstract val name: String

    @CallSuper
    open fun init() {
        loadUserCommand()
    }

    abstract fun isSupport(command: String): Boolean
    abstract fun doAction(command: String)

    internal val userCommands = mutableSetOf<String>()

    val configs: MutableMap<String, String> = mutableMapOf()

    fun loadUserCommand() {
        userCommands.clear()
        AppConfig.homeSystemUserCommand?.lines()?.forEach {
            it.trim().takeIf { i -> i.isNotEmpty() }?.also { s ->
                userCommands.add(s)
            }
        }
    }

    abstract fun test()

    //用于显示
    abstract fun summary(): String


    protected open fun inUserCommand(command: String, vararg extras: Any): Boolean {
        return userCommands.any {
            it.toParamRegex().match(command) != null
        }
    }

    //保存配置到指令存储
    abstract fun saveInstConfig()

    internal fun parseConfig(s: String) {
        configs.clear()
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
        if (BuildConfig.DEBUG) {
            GlobalLog.log(GsonHelper.toJson(configs, true))
        }
    }

    abstract fun getSettingItems(context: Context): Array<SettingChildItem>

    companion object {
        fun load(type: Int?): ISmartHomeSystem? {
            return when (type) {
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
                    webHookUrl=${rokidSettings?.getString("rokid_url") ?: if (BuildConfig.DEBUG)
                    "https://homebase.rokid.com/trigger/with/j1WW9-f0sR" else ""}
                    #设备序列号
                    deviceId=${rokidSettings?.getString("rokid_device_id")
                    ?: if (BuildConfig.DEBUG) "0201021716000353" else ""}
                    #后台管理地址，用于获取房间列表和设备列表
                    adminUrl=${if (BuildConfig.DEBUG) "http://baduxiyang.oicp.net:9001" else ""}
                    #后台管理登录名
                    username=${if (BuildConfig.DEBUG) "admin" else ""}
                    #后台管理登录密码
                    pass=${if (BuildConfig.DEBUG) "admin" else ""}
                    #实时位置服务后台地址
                    realTimeLocUrl=${if (BuildConfig.DEBUG) "http://baduxiyang.oicp.net:9001/api/globalVariables/user0gps" else ""}
                    #发送完成提示词
                    finishWord=若琪指令发送成功
                    """.trimIndent()
            } else ""
        }
    }
}