package cn.vove7.common

import cn.vove7.common.model.ExResult
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.system.DeviceInfo

interface SystemOperation {
    /**
     * 通过包名打开App
     */
    fun openAppByPkg(pkg: String): ExResult<String>

    /**
     * 通过通过关键字匹配
     * @return pkgName if ok
     */
    fun openAppByWord(appWord: String): ExResult<String>

    /**
     * 拨打
     */
    fun call(s: String): ExResult<String>

    /**
     * 手电
     */
    fun openFlashlight(): ExResult<Any>

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
     * 打开链接
     */
    fun openUrl(url: String)

    //TODO
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
    fun volumeUp()
    fun volumeDown()
    fun setMusicVolume(index: Int)
    fun setAlarmVolume(index: Int)
    fun setNotificationVolume(index: Int)
    fun isMediaPlaying(): Boolean

    //最大音量
    var musicMaxVolume: Int
    //当前音量
    var musicCurrentVolume: Int

    fun vibrate(millis: Long): Boolean
    fun vibrate(arr: Array<Long>): Boolean
}