package cn.vove7.common.net.model

import cn.vove7.common.model.UserInfo
import java.io.Serializable

/**
 * # BaseRequestModel
 *
 * @author Administrator
 * 2018/9/16
 */
class BaseRequestModel<T : Any>(var body: T? = null, val arg1: String? = null)
    : Serializable {
    val userId = UserInfo.getUserId()

}