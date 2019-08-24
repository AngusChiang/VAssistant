package cn.vove7.common.bridges

import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import cn.vove7.common.annotation.ScriptApiClass
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.system.DeviceInfo
import java.io.File

//TODO 注解API
@ScriptApiClass("system")
interface SystemOperation {
    /**
     * 打开应用详情页
     * @param pkg String 包名
     * @return Boolean
     */
    fun openAppDetail(pkg: String): Boolean

    /**
     * 获得App启动的Intent
     * @param pkg String
     * @return Intent?
     */
    fun getLaunchIntent(pkg: String): Intent?

    /**
     * 根据App名，获取应用包名
     * 标记 -> 应用列表
     * @param appWord String App名/别名
     * @return String? 包名
     */
    fun getPkgByName(appWord: String, excludeUnstartable: Boolean = true): String?

    /**
     * 同getPkgBName
     * 已弃用
     * @param appWord String
     * @return String?
     */
    fun getPkgByWord(appWord: String): String?

    /**
     * 模糊匹配 标记本地
     * @param name String
     * @return String? first: 匹配的联系人姓名 or 纯数字 second: 手机号
     */
    fun getPhoneByName(name: String): String?

    /**
     * 模糊匹配联系人 包含实际联系人
     * @param name String
     * @return Pair<String,String>?
     */
    fun getContactByName(name: String): Pair<String, Array<String>>?

    /**
     * 通过包名打开App
     *
     * @param pkg String
     * @param clearTask Boolean 是否重置Activity栈（重置后可进入App首页）
     * @return Boolean
     */
    fun openAppByPkg(pkg: String, clearTask: Boolean = false): Boolean

    /**
     * 通过getPkgByWord 打开pkg
     * @return pkgName if ok, else null
     */
    fun openAppByWord(appWord: String): String?


    /**
     * 拨打电话
     * @param s 纯数字电话
     */
    fun call(s: String): Boolean

    fun call(s: String, simId: Int? = null): Boolean

    /**
     * 闪光灯
     */
    fun openFlashlight(): Boolean

    fun closeFlashlight(): Boolean

    /**
     * 获取手机信息
     */
    fun getDeviceInfo(): DeviceInfo

    /**
     * 获取App信息
     * @param s 包名 或 App 名
     */
    fun getAppInfo(s: String): AppInfo?

    /**
     * 是否安装此应用
     * @param pkg String 包名
     * @return Boolean
     */
    fun hasInstall(pkg: String): Boolean

    /**
     * 打开链接
     */
    fun openUrl(url: String): Boolean

    fun sendKey(keyCode: Int)

    //Media control
    fun mediaPause()

    fun mediaStart()
    fun mediaResume()
    fun mediaStop()
    fun mediaNext()
    fun mediaPre()
    fun volumeMute()
    fun volumeUnmute()
    /**
     * 勿扰模式
     */
    fun doNotDisturbMode()


    fun volumeUp()
    fun volumeDown()
    fun setMusicVolume(index: Int)
    fun setAlarmVolume(index: Int)
    fun setNotificationVolume(index: Int)
    fun isMediaPlaying(): Boolean

    //最大音量
    val musicMaxVolume: Int
    //当前音量
    val musicCurrentVolume: Int

    fun vibrate(millis: Long): Boolean
    fun vibrate(arr: Array<Long>): Boolean

    fun openBluetooth(): Boolean
    fun closeBluetooth(): Boolean
    fun openWifi(): Boolean
    fun closeWifi(): Boolean
    fun openWifiAp(): Boolean
    fun closeWifiAp(): Boolean

    fun isScreenOn(): Boolean

    fun getClipText(): String?
    fun setClipText(text: String?) :Boolean

    fun sendEmail(to: String, subject: String? = null, content: String? = null)

    fun lockScreen(): Boolean
    fun screenShot(): Bitmap?
    fun screen2File(): File?

    /**
     * 分享文字
     * @param content String?
     */
    fun shareText(content: String?)

    /**
     * 分享图片
     * @param imgPath String? 图片路径
     */
    fun shareImage(imgPath: String?)

    /**
     * 获取用户地理位置
     * 需授权
     * @return String? 获取失败返回空
     */
    fun location(): Location?

    /**
     * 获得内外ip
     * @return String?
     */
    fun getLocalIpAddress(): String?

    /**
     * 获得外网ip
     * @return String?
     */
    fun getNetAddress(): String?

    /**
     * 创建系统闹钟
     * @param message String? 备注
     * @param day Int? 周 重复  1日 - 7六
     * @param hour Int 小时
     * @param minutes Int 分钟
     * @param noUi 不显示闹钟界面
     */
    fun createAlarm(message: String?, days: Array<Int>?, hour: Int, minutes: Int, noUi: Boolean): Boolean

    /**
     * 创建日历事件
     * @param title String
     * @param content String?
     * @param beginTime Long
     * @param endTime Long?
     * @param isAlarm Boolean
     */
    fun createCalendarEvent(title: String, content: String?, beginTime: Long, endTime: Long? = null, isAlarm: Boolean)

    fun createCalendarEvent(title: String, content: String?, beginTime: Long, endTime: Long? = null, earlyAlarmMinute: Long? = null)

//    fun hideInputMethod()

    fun screenOn()
    fun screenOff()
    fun quickSearch(s: String?)
    fun disableSoftKeyboard(): Boolean
    fun enableSoftKeyboard(): Boolean

    /**
     * 发送短信
     * @param phone String
     * @param content String
     */
    fun sendSMS(phone: String, content: String)

    /**
     * 电量
     */
    val batteryLevel: Int

    /**
     * 充电状态
     * 脚本对应：isCharging()
     */
    val isCharging: Boolean

    /**
     * 插入的sim卡数量
     */
    val simCount: Int

    /**
     * 获得联系人数组  元素：Pair(contactName,phone)
     */
    val contacts: Array<Pair<String, String?>>

    /**
     * 保存到标记联系人
     * @param name String
     * @param regex String?
     * @param phone String
     */
    fun saveMarkedContact(name: String, regex: String, phone: String): Boolean

    /**
     * 保存到标记应用
     * @param name String
     * @param regex String?
     * @param pkg String
     */
    fun saveMarkedApp(name: String, regex: String, pkg: String): Boolean

    fun enableNfc()

    fun disableNfc()

    /**
     * 强行停止应用
     * @param pkg String
     */
    fun killApp(pkg: String): Boolean

    /**
     * 屏幕亮度
     * 范围：0～255
     */
    var screenBrightness: Int

    /**
     * Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC：值为1，自动调节亮度。
     * Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL：值为0，手动模式。
     */
    var screenBrightnessMode: Int
}