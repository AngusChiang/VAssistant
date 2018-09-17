package cn.vove7.common.netacc.tool

import cn.vove7.common.netacc.model.ResponseMessage
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

/**
 * # OneGson
 *
 * @author 17719247306
 * 2018/9/13
 */
object OneGson {

    fun <T> fromJsonObj(s: String?, type: Type): ResponseMessage<T>? {
        val bean = GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create().fromJson<ResponseMessage<T>>(s, type)
        return bean
    }


}