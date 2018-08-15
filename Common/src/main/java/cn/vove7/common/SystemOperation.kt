package cn.vove7.common

import cn.vove7.common.model.ExResult
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
     * 打开链接
     */
    fun openUrl(url: String)

}