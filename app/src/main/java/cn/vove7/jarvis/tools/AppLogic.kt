package cn.vove7.jarvis.tools

import cn.vove7.common.BuildConfig
import cn.vove7.common.model.UserInfo

/**
 * # Logic
 * 负责App内逻辑
 * @author Vove
 * 2019/7/1
 */
object AppLogic {

    /**
     * 是否可以使用讯飞
     * @return Boolean
     */
    fun canXunfei(): Boolean {
        return UserInfo.isPermanentVip() && !BuildConfig.DEBUG
    }

}