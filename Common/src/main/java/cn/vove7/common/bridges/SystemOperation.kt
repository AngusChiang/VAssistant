package cn.vove7.common.bridges

import android.graphics.Bitmap
import android.location.Location
import cn.vove7.common.model.ExResult
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.system.DeviceInfo
import java.io.File

interface SystemOperation {
    /**
     * 打开应用详情页
     * @param pkg String 包名
     * @return Boolean
     */
    fun openAppDetail(pkg: String): Boolean

    /**
     * 根据App名，获取应用包名
     * 标记 -> 应用列表
     * @param appWord String App名/别名
     * @return String?
     */
    fun getPkgByWord(appWord: String): String?

    /**
     * 通过包名打开App
     *
     * @param pkg String
     * @param resetTask Boolean 是否重置Activity栈（重置后可进入App首页）
     * @return Boolean
     */
    fun openAppByPkg(pkg: String, resetTask: Boolean = false): Boolean

    /**
     * 通过getPkgByWord 打开pkg
     * @return pkgName if ok, else null
     */
    fun openAppByWord(appWord: String): String?


    /**
     * 拨打电话
     * @param s 纯数字电话/联系人/标记联系人
     */
    fun call(s: String): ExResult<String>

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
     * 打开链接
     */
    fun openUrl(url: String)

    //fixme
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

    fun openBluetooth(): Boolean
    fun closeBluetooth(): Boolean
    fun openWifi(): Boolean
    fun closeWifi(): Boolean
    fun openWifiAp(): Boolean
    fun closeWifiAp(): Boolean

    fun isScreenOn(): Boolean

    fun getClipText(): String?
    fun setClipText(text: String?)

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
    fun shareImage(imgPath:String?)

    /**
     * 获取用户地理位置
     * 需授权
     * @return String? 获取失败返回空
     */
    fun location(): Location?

    fun getIpAddress(): String?

}