package cn.vove7.common

interface SystemOperation {
    /**
     * 通过包名打开App
     */
    fun openAppByPkg(pkg: String): Boolean

    /**
     * 通过通过关键字匹配
     */
    fun openAppByWord(appWord: String): String?

    /**
     * 拨打
     */
    fun call(s: String): String?

    /**
     * 手电
     */
    fun openFlashlight(): Boolean
}